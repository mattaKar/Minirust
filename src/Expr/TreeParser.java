package Expr;

import Exceptions.*;
import Objets.*;

import org.antlr.runtime.tree.CommonTree;
import org.antlr.stringtemplate.language.Cat;

import java.util.ArrayList;

public class TreeParser {
	public static int LIGNE = 0;

	public static void analyseRec(Tables tables, CommonTree t, TableDesSymboles tds, Offsets offsets) throws Exception {
		TreeParser.LIGNE = t.getLine();
		if (t.isNil()) {
			int numberMain = 0;
			if (t.getText() == null) {
				for (int i = 0; i < t.getChildCount(); i++) {
					if (t.getChild(i).getChild(0).getText().equals("main")) {
						numberMain = numberMain + 1;
					}
				}
				try {
					oneMain(numberMain);
					for (int i = 0; i < t.getChildCount(); i++) {
						TreeParser.analyseRec(tables, (CommonTree) t.getChild(i), tds, offsets);
					}

				} catch (NoMain e) {
					e.printStackTrace();
				}
			}
		} else {
			int nbChilds = t.getChildCount();
			boolean pointeur = false;
			String type = "";

			switch (t.getText()) {
				case "DECL":
					String name;
					boolean mut = false;
					int posName = 0;
					String value = null;

					for (int i = 0; i < nbChilds; i++) {
						if (t.getChild(i).getText().equals("mut")) {
							mut = true;
							posName = posName + 1;
						}

						if (t.getChild(i).getText().equals("SPE_UNAIRE"))
							posName = i;
					}

					if (t.getChild(posName).getText().equals("SPE_UNAIRE")) {
						name = t.getChild(posName).getChild(1).getText();
						pointeur = true;
					} else
						name = t.getChild(posName).getText();

					if (t.getChild(nbChilds - 1).getChildCount() > 0) { // AFFECT
						CommonTree node = (CommonTree) t.getChild(nbChilds - 1);

						if (TreeParser.isInteger(node.getChild(0).getText())
								|| TreeParser.isBoolean(node.getChild(0).getText())) {
							value = node.getChild(0).getText();
						}
						type = TreeParser.analyseExp((CommonTree) node.getChild(0), tds);
					}

					CommonTree vecNode = (CommonTree) t.getChild(nbChilds - 1).getChild(0);

					if (!vecNode.getText().equals("VEC")) {
						if (type != null) {
							if (type.equals("bool") || type.equals("i32") || type.equals("& bool")
									|| type.equals("& i32")) {
								tds.ajouterVariable(name, mut, type, value, pointeur, false,
										TreeParser.getDeplacement(name, type, tds), offsets);
							} else {
								ArrayList<String> structureVariables = new ArrayList<>();
								ArrayList<ArrayList<String>> structureValeurs = new ArrayList<>();

								for (int i = 0; i < vecNode.getChildCount(); i++) {
									structureVariables.add(vecNode.getChild(i).getChild(0).getText());
									CommonTree val = (CommonTree) vecNode.getChild(i).getChild(1);
									ArrayList<String> valeurs = new ArrayList<>();
									String valeurType = null;

									if (!val.getText().equals("VEC"))
										valeurs.add(val.getText());
									else
										for (int j = 0; j < val.getChildCount(); j++) {
											String valeur = val.getChild(j).getText();

											if (j >= 1 && !TreeParser.findType(valeur).equals(valeurType))
												throw new InvalidVecteurVariableType(val.getParent().getParent().getText(), valeurType, valeur, findType(valeur));
											else
												valeurType = TreeParser.findType(valeur);

											valeurs.add(valeur);
										}

									structureValeurs.add(valeurs);
								}
								int deplacement_tot = 0;
								ArrayList<Integer> deplacements = new ArrayList<>();
								try {
									for (int i = 0; i < structureVariables.size(); i++) {
										String type2 = findType(structureValeurs.get(i).get(0));
										int deplacement_var = getDeplacement(structureVariables.get(i), type2, tds)
												* structureValeurs.get(i).size();
										deplacements.add(deplacement_var);
										deplacement_tot += deplacement_var;
									}
								} catch (Exception e) {
								}
								tds.ajouterStructureVariable(name, type, structureVariables, structureValeurs, pointeur,
										deplacements, deplacement_tot, offsets);
							}
						}
					} else {
						int nbChild2 = vecNode.getChildCount();
						ArrayList<String> values = new ArrayList<>();
						String vecType = findType(vecNode.getChild(0).getText());
						try {
							if (nbChild2 > 1) {
								for (int i = 0; i < nbChild2 - 1; i++) {
									isSameType(vecNode.getChild(i + 1).getText(), findType(vecNode.getChild(i).getText()),
											findType(vecNode.getChild(i + 1).getText()));
								}
							}
							for (int i = 0; i < nbChild2; i++)
								values.add(vecNode.getChild(i).getText());

							tds.ajouterVecteur(name, vecType, values, false, false,
									getDeplacement(name, vecType, tds) * nbChild2, offsets);
						} catch (InvalidTypeArgument e) {
							e.printStackTrace();
						}
					}
					break;
				case "STRUCT":
					String nameStruct = t.getChild(0).getText();
					ArrayList<String> varNames = new ArrayList<>();
					ArrayList<String> varTypes = new ArrayList<>();
					ArrayList<Boolean> varPointeurs = new ArrayList<>();
					ArrayList<Boolean> vecteurs = new ArrayList<>();

					TreeParser.fillVarNamesTypes(1, t, varNames, varTypes, varPointeurs);

					for (int i = 0; i < varNames.size(); i++)
						vecteurs.add(varTypes.get(i).split(" ")[0].equals("VEC"));

					tds.ajouterStructure(nameStruct, varNames, varTypes, varPointeurs, vecteurs);
					break;
				case "BLOC":
					if (!t.getParent().getText().equals("FUNC")) {
						TableDesSymboles tds2 = new TableDesSymboles(tables, tds);

						for (int i = 0; i < nbChilds; i++)
							TreeParser.analyseRec(tables, (CommonTree) t.getChild(i), tds2, offsets);
					} else
						for (int i = 0; i < nbChilds; i++)
							TreeParser.analyseRec(tables, (CommonTree) t.getChild(i), tds, offsets);

					break;
				case "IF":
					TreeParser.analyseExp((CommonTree) t.getChild(0), tds);

					for (int i = 1; i < t.getChildCount(); i++)
						TreeParser.analyseRec(tables, (CommonTree) t.getChild(i), tds, offsets);
					break;
				case "ELSE":
					TreeParser.analyseRec(tables, (CommonTree) t.getChild(0), tds, offsets);
					break;
				case "WHILE":
					TreeParser.analyseExp((CommonTree) t.getChild(0), tds);
					TreeParser.analyseRec(tables, (CommonTree) t.getChild(1), tds, offsets);
					break;
				case "FUNC":
					String nameFunc = t.getChild(0).getText();
					String returnType = null;
					Arguments args = null;
					ArrayList<String> argNames;
					ArrayList<String> argTypes;
					ArrayList<Boolean> argPointeurs;

					for (int i = 1; i < t.getChildCount(); i++) {
						CommonTree node = (CommonTree) t.getChild(i);
						switch (node.getText()) {
							case "FUNC_ARGS":
								argNames = new ArrayList<>();
								argTypes = new ArrayList<>();
								argPointeurs = new ArrayList<>();

								TreeParser.fillVarNamesTypes(0, node, argNames, argTypes, argPointeurs);

								args = new Arguments(argNames, argTypes, argPointeurs);

								break;
							case "BLOC":
								/*
								 * if (!returnTypeFunc.equals("void")) { try {
								 * isReturn(node); goodReturnType(node); } catch
								 * (InvalidTypeReturn e) { e.printStackTrace(); } catch
								 * (NoReturn e) { e.printStackTrace(); } }
								 */
								tds.ajouterFonction(nameFunc, returnType, args, offsets);
								TableDesSymboles tds2 = new TableDesSymboles(tables, tds);

								if (args != null) {
									ArrayList<String> names = args.getNames();
									ArrayList<String> types = args.getTypes();

									for (int k = 0; k < names.size(); k++) {
										String[] differentTypes = types.get(k).split(" ");
										Structure structure;
										name = names.get(k);

										if (differentTypes[0].equals("&")) {
											pointeur = true;
											type = types.get(k).substring(2, types.get(k).length());
										} else
											type = types.get(k);

										if (differentTypes[0].equals("VEC")) {
											String types2 = differentTypes[1];

											for (int j = 2; i < differentTypes.length; i++)
												types2 = types2.concat(differentTypes[j]);

											tds2.ajouterVecteur(name, types2, null, pointeur, true, getDeplacement(name, types2, tds) * names.size(), offsets);
										} else if ((structure = tds.getStructure(tds, differentTypes[0], false)) != null)
											tds2.ajouterArgumentStructure(name, structure, pointeur, offsets);
										else {
											tds2.ajouterVariable(name, true, type, null, pointeur, true, getDeplacement(name, type, tds), offsets);
										}
									}
								}

								TreeParser.analyseRec(tables, node, tds2, offsets);
								break;
							default:
								returnType = node.getText();
								break;
						}
					}
					break;
				case "PRINT":
					analyseExp((CommonTree) t.getChild(0), tds);
					break;
				case "RETURN":
					String returnTypeFunc;
					CommonTree p = t;
					while (!(p.getParent().getText().equals("FUNC")))
						p = (CommonTree) p.getParent();

					p = (CommonTree) p.getParent(); // On remonte au noeud FUNC de
					// declaration de la fonction
					String name1 = p.getChild(0).getText();
					if (p.getChildCount() == 4) {
						returnTypeFunc = p.getChild(p.getChildCount() - 2).getText();
					} else if (p.getChildCount() == 3) {
						if (!p.getChild(p.getChildCount() - 2).getText().equals("FUNC_ARGS")) {
							returnTypeFunc = p.getChild(p.getChildCount() - 2).getText();
						} else {
							returnTypeFunc = "void";
						}
					} else {
						returnTypeFunc = "void";
					}
					if (!name1.equals("main")) {
						try {

							if (!(returnTypeFunc.equals("void"))) {
								goodReturnType(analyseExp((CommonTree) t.getChild(0), tds), returnTypeFunc);
								analyseExp((CommonTree) t.getChild(0), tds);
							}
						} catch (InvalidTypeReturn e) {
							e.printStackTrace();
						}
					} else {
						if (t.getChild(0).getChildCount() > 0) {
							analyseExp((CommonTree) t.getChild(0).getChild(0),tds);
							System.out.println(t.getChild(0).getChild(0).getText());
						}
						analyseExp((CommonTree) t.getChild(0), tds);
					}
					break;
			}
		}
	}

	private static String analyseExpUnaire(CommonTree t, String spe_unaire, TableDesSymboles tds) throws Exception {
		String type;

		switch (spe_unaire) {
			case "-":
				type = "i32";
				return type.equals(analyseExp(t, tds)) ? type : null;
			case "!":
				type = "bool";
				return type.equals(analyseExp(t, tds)) ? type : null;
			case "&":
				return analyseExp(t, tds);
			case "*":
				return analyseExp(t, tds);
			default:
				return null;
		}

	}

	public static String analyseExp(CommonTree t, TableDesSymboles tds) throws Exception {
		String s = t.getText();
		switch (s) {
			case "SPE_UNAIRE":
				return analyseExpUnaire((CommonTree) t.getChild(1), t.getChild(0).getText(), tds);
			case "+":
			case "-":
			case "*":
			case "/":
			case "<":
			case "<=":
			case ">":
			case ">=":
			case "==":
			case "!=":
			case "&&":
			case "||":
				String fg = TreeParser.analyseExp((CommonTree) t.getChild(0), tds);
				String fd = TreeParser.analyseExp((CommonTree) t.getChild(1), tds);

				try {
					TreeParser.isSameTypeCalcul(t.getChild(0).getText(), fg, t.getChild(1).getText(), fd);
				} catch (InvalidTypeCalcul e) {
					e.printStackTrace();
				}

				if (fg != null && fg.equals(fd)) {
					switch (s) {
						case "+":
						case "-":
						case "*":
						case "/":
							return "i32";
						case "<":
						case "<=":
						case ">":
						case ">=":
						case "==":
						case "!=":
						case "&&":
						case "||":
							return "bool";
					}
				}

			default:
				Structure structure;
				Variable variable;
				Vecteur vecteur;

				if (isInteger(t.getText()))
					return "i32";
				else if (isBoolean(t.getText()))
					return "bool";

				else if (t.getChildCount() == 0) {
					try {
						if((variable = tds.getVariable(tds, t.getText())) == null)
							throw new NonExistantVariable(t.getText());
						else if(variable.getStructure(false) != null)
							return  variable.getStructure(false).getStructureName();
						else return variable.getType();
					} catch (NonExistantVariable e) {
						e.printStackTrace();
					}
				} else if ((structure = tds.getStructure(tds, t.getText(), false)) != null)
					return structure.getStructureName();
				else if ((vecteur = tds.getVecteur(tds, t.getText(), false)) != null)
					return vecteur.getType();

				else if (t.getChildCount() != 0) {
					CommonTree child = (CommonTree) t.getChild(0);

					if (t.getText().equals("BLOC"))
						return tds.getFonction(tds, child.getText()).getReturnType();
					else if (child.getText().equals("IND")) {
						String name = t.getText();
						t = (CommonTree) child.getChild(0);
						try {
							Vecteur vect = tds.getVecteur(tds, name, true);
							int taillemax = vect.getValeurs().size();
							if (findType(child.getChild(0).getText()).equals("i32")) {
								isValidIndex(taillemax, child.getChild(0).getText(), name);
							} else if (child.getChild(0).getChildCount() == 0) {
								variable = tds.getVariable(tds, child.getChild(0).getText());
								isValidIndex(taillemax, variable.getValeur(), name);
							}

							if (t.getChildCount() == 0)
								try {
									String variable2 = TreeParser.analyseExp(t, tds);
									TreeParser.isSameType(name, "i32", variable2);

								} catch (InvalidTypeArgument e) {
									e.printStackTrace();
								}
							else if (child.getText().equals("IND"))

								try {
									TreeParser.analyseExp(t, tds);
									Vecteur vectFils = ((TableDesVecteurs) (tds.get(TableType.VEC))).getVecteur(tds,
											t.getText());
									isSameTypeVecteurVariable(vect.getName(), "i32", vectFils.getName(),
											vectFils.getType());
								} catch (InvalidVecteurVariableType | NonExistantVecteur e) {
									e.printStackTrace();
								}
							else if (child.getText().equals("CALL_ARGS"))
								try {
									Fonction fonctionFils = tds.getFonction(tds, t.getText());
									isSameTypeVecteurVariable(vect.getName(), "i32", fonctionFils.getName(),
											fonctionFils.getReturnType());
								} catch (InvalidVecteurVariableType | NonExistantFunction e) {
									e.printStackTrace();
								}
						} catch (NonExistantVecteur | IndexOutOfBounds e) {
							e.printStackTrace();
						}
					} else if (child.getText().equals("CALL_ARGS")) {
						String name1 = t.getText();
						try {
							Fonction fonc = tds.getFonction(tds, name1);
							t = child;
							int nbChilds2 = t.getChildCount();
							try {
								fonc.validNumberArgs(fonc, nbChilds2);

								for (int i = 0; i < nbChilds2; i++) {
									String theoricalType = fonc.getArgs().getTypes().get(i);
									CommonTree Child = (CommonTree) t.getChild(i);

									if (Child.getChild(0).getChildCount() == 0) {
										try {
											TreeParser.isSameTypeFunc(name1, theoricalType, Child.getChild(0).getText(),
													analyseExp((CommonTree) Child.getChild(0), tds));

											/*
											 * try { boolean theoricalPointerType =
											 * fonc.getArgs().getPointeurs().get
											 * (i); char pointertest[] = null;
											 *
											 * nameVal.getChars(0, 0, pointertest,
											 * 0); TreeParser.isSamePointerType(
											 * theoricalPointerType,
											 * pointertest[0]);
											 *
											 * if (fonc.getReturnType() == null) {
											 * return null; } } catch
											 * (PointerTypeException
											 * pointeurTypeException) {
											 *
											 * }
											 */
										} catch (InvalidTypeArguments e) {
											e.printStackTrace();
										}
									} else if (Child.getChild(0).getText().equals("IND"))
										try {

											TreeParser.analyseExp(Child, tds);
											Vecteur vectFils = ((TableDesVecteurs) (tds.get(TableType.VEC))).getVecteur(tds,
													t.getText());
											isSameType(fonc.getName(), theoricalType, vectFils.getType());
											int taillemax = vectFils.getValeurs().size();
											if (findType(child.getChild(0).getText()).equals("i32")) {
												isValidIndex(taillemax, child.getChild(0).getText(), name1);
											}
										} catch (NonExistantVecteur | InvalidTypeArgument | IndexOutOfBounds e) {
											e.printStackTrace();
										}
									else if (Child.getChild(0).getText().equals("CALL_ARGS"))
										try {
											TreeParser.analyseExp(Child, tds);
											TreeParser.isSameType(fonc.getName(), theoricalType,
													tds.getFonction(tds, t.getText()).getReturnType());
										} catch (NonExistantFunction | InvalidTypeArgument e) {
											e.printStackTrace();
										}
									else
										TreeParser.analyseExp(Child, tds);
								}

								return fonc.getReturnType();
							} catch (InvalidArgumentsNumber e) {
								e.printStackTrace();
							}
						} catch (NonExistantFunction e) {
							e.printStackTrace();
						}
					} else if (child.getText().equals("ASSOC")) {
						String name = t.getText();
						t = (CommonTree) child.getChild(0);

						try {
							Structure struct = tds.getVariable(tds, name).getStructure(false);

							// if((struct = tds.getVariable(tds,
							// name).getStructure()) == null)
							// struct = tds.getVecteur(tds, name)

							int k = 0;

							for (int j = 0; j < struct.getNames().size(); j++) {
								try {
									isSameName(struct.getNames().get(j), t.getText(), struct.getStructureName(), k);

									if (j == struct.getNames().size() - 1)
										k = 1;
								} catch (InvalidStructureVarName e) {
									e.printStackTrace();
								}
							} /*
							 * else if (child.getText().equals("IND")) { try {
							 * TreeParser.analyseExp(t, tds); Vecteur vectFils =
							 * ((TableDesVecteurs)
							 * (tds.get(TableType.VEC))).getVecteur(tds,
							 * t.getText());
							 * isSameTypeVecteurVariable(vect.getName(), "i32",
							 * vectFils.getName(), vectFils.getType()); } catch
							 * (InvalidVecteurVariableType e) { } catch
							 * (NonExistantVecteur e2) { } } else if
							 * (child.getText().equals("CALL_ARGS")) { try {
							 * Fonction fonctionFils = ((TableDesFonctions)
							 * (tds.get(TableType.FONC))).getFonction(tds,
							 * t.getText());
							 * isSameTypeVecteurVariable(vect.getName(), "i32",
							 * fonctionFils.getName(),
							 * fonctionFils.getReturnType()); } catch
							 * (InvalidVecteurVariableType e) { } catch
							 * (NonExistantFunction e2) {
							 *
							 * } } } catch (NonExistantStructure e) { }
							 */
						} catch (NonExistantStructure e) {
							e.printStackTrace();
						}
					}
					else if (t.getText().equals("IND")) {
						String name = t.getParent().getText();
						//	t = (CommonTree) child.getChild(0);
						try {
							Vecteur vect = tds.getVecteur(tds, name, true);
							int taillemax = vect.getValeurs().size();
							if (findType(t.getChild(0).getText()).equals("i32")) {
								isValidIndex(taillemax, t.getChild(0).getText(), name);
							} else if (t.getChild(0).getChildCount() == 0) {
								variable = tds.getVariable(tds, t.getChild(0).getText());
								isValidIndex(taillemax, variable.getValeur(), name);
							}
						}
						catch (IndexOutOfBounds e) {
							e.printStackTrace();
						}
					}
				}
		}

		return null;
	}

	private static void fillVarNamesTypes(int startIndex, CommonTree node, ArrayList<String> varNames,
										  ArrayList<String> varTypes, ArrayList<Boolean> varPointeur) {
		for (int i = startIndex; i < node.getChildCount(); i++) {
			CommonTree node2 = (CommonTree) node.getChild(i);

			String name = node2.getChild(0).getText();
			String temp = node2.getChild(1).getText();
			String type = temp;

			if (temp.equals("&")) {
				varPointeur.add(true);
				type = type.concat(" ");
				type = type.concat(node2.getChild(2).getText());
				node2 = (CommonTree) node2.getChild(2);
			} else {
				varPointeur.add(false);
				node2 = (CommonTree) node2.getChild(1);
			}

			while (temp.equals("VEC")) {
				node2 = (CommonTree) node2.getChild(0);

				temp = node2.getText();
				type = type.concat(" ");
				type = type.concat(temp);
			}

			varNames.add(name);
			varTypes.add(type);
		}
	}

	public static String findType(String value) {
		try {
			Integer.parseInt(value);
			return "i32";
		} catch (NumberFormatException e) {
			return isBoolean(value) ? "bool" : "";
		}
	}

	private static void isSameType(String name, String theoricalType, String realType) throws InvalidTypeArgument {
		if (realType == null)
			throw new InvalidTypeArgument(name, theoricalType, "null");
		if (!(theoricalType.equals(realType)))
			throw new InvalidTypeArgument(name, theoricalType, realType);
	}

	private static void isSameTypeFunc(String name, String theoricalType, String name2, String realType)
			throws InvalidTypeArguments {
		if (realType == null)
			throw new InvalidTypeArguments(name, name2, theoricalType, "null");
		if (!(theoricalType.equals(realType)))
			throw new InvalidTypeArguments(name, name2, theoricalType, realType);
	}

	private static void isSameTypeVecteurVariable(String vecteurName, String vecteurType, String variableName,
												  String variableType) throws InvalidVecteurVariableType {
		if (variableType == null)
			throw new InvalidVecteurVariableType(vecteurName, vecteurType, variableName, "null");
		else if (!(vecteurType.equals(variableType)))
			throw new InvalidVecteurVariableType(vecteurName, vecteurType, variableName, variableType);
	}

	private static void isSameTypeCalcul(String name, String theoricalType, String name2, String realType)
			throws InvalidTypeCalcul {
		if (realType == null)
			throw new InvalidTypeCalcul(name, theoricalType, name2, "null");
		else if (theoricalType == null)
			throw new InvalidTypeCalcul(name, "null", name2, realType);
		else if (!(theoricalType.equals(realType)))
			throw new InvalidTypeCalcul(name, theoricalType, name2, realType);
	}

	private static void isSamePointerType(Boolean VV, char etoile, String var) throws PointerTypeException {
		if ((!VV || etoile != '*') && (VV || etoile == '*'))
			throw new PointerTypeException(VV, var);
	}

	private static boolean isBoolean(String str) {
		if (str != null)
			return (str.equals("true") || str.equals("false"));

		return false;
	}

	private static void isSameName(String str1, String str2, String name, int parcours) throws InvalidStructureVarName {
		if (parcours == 1)
			throw new InvalidStructureVarName(name, str1, str2);
	}

	private static void oneMain(int num) throws NoMain {
		if (num != 1)
			throw new NoMain();
	}

	private static Boolean isInteger(String str) {
		return findType(str).equals("i32");
	}

	private static void goodReturnType(String analyseExp, String returnTypeFunc) throws InvalidTypeReturn {
		if (!analyseExp.equals(returnTypeFunc))
			throw new InvalidTypeReturn(analyseExp, returnTypeFunc);
	}

	private static void isValidIndex(int taillemax, String text, String name) throws IndexOutOfBounds {
		if (Integer.parseInt(text) >= taillemax)
			throw new IndexOutOfBounds(name);
	}

	public static int getDeplacement(String name, String type, TableDesSymboles tds) {
		if (type != null) {
			switch (type) {
				case "i32":
					return 4;
				case "bool":
					return 1;
				default:
					return 100;
			}
		}
		return 0;
	}
}

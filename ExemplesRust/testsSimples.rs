struct triangle {
    cote1 : Vec<i32>,
    cote2 : i32,
    cote3 : i32
}

fn main(w : i32, x: bool, y: triangle, z: bool) -> Vec<i32> {
    let b = 0;
    let c = 1;
    let d = c;
    let e = (1 + 4) * 3;
    let mut f = (1 + 4) * e;
    let h = main(4,true,triangle { cote1: vec![0, 1, 2, 3], cote2: 20, cote3: 30 }, false);
    let mut i = true;
    let j = i && false;
    let i = false;
    let k = 3 <= 4;
    let l = -true;
    let m = !4;
    let n = !true;
    let o = vec![true, false, false, true];
    let p = *e ;
    let q = vec![1, 2, 3];
    let r = 5;
}

fn test() -> bool {

}
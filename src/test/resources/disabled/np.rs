use std::ptr;

unsafe fn _test() {
        let p: *const i32 = ptr::null();
        std::ptr::read(p);
}

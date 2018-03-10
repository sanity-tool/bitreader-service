struct List {
    struct List *next;
} *PLIST;

static void switchToNext() {
    PLIST = PLIST->next;
}
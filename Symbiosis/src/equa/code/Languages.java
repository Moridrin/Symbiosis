package equa.code;

public enum Languages {

    JAVA(Language.JAVA, true, false, false),
    CSHARP(Language.CSHARP, false, false, false);

    private boolean library, orm, mInh;
    private Language l;

    Languages(Language l, boolean library, boolean orm, boolean mInh) {
        this.library = library;
        this.orm = orm;
        this.mInh = mInh;
        this.l = l;
    }

    public boolean library() {
        return library;
    }

    public boolean orm() {
        return orm;
    }

    public boolean mInh() {
        return mInh;
    }

    public Language language() {
        return l;
    }
}

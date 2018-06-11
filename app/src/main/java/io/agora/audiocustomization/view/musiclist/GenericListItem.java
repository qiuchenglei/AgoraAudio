package io.agora.audiocustomization.view.musiclist;

public class GenericListItem {
    public GenericListItem(int id, int res, String name, String desc) {
        this.mId = id;

        this.mIconRes = res;
        this.mName = name;

        this.mDesc = desc;
    }

    public GenericListItem(int id, int res, String desc) {
        this(id, res, null, desc);
    }

    public GenericListItem(int id, int res) {
        this(id, res, null);
    }

    public final int mId;

    public final int mIconRes;
    public final String mName;

    public final String mDesc;
}

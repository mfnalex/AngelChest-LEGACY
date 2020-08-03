package de.jeff_media.AngelChest;

public class PendingConfirm {

    AngelChest chest;
    Action action;
    enum Action {
        TP, Fetch
    }

    PendingConfirm(AngelChest chest, Action action) {
        this.chest=chest;
        this.action=action;
    }

}

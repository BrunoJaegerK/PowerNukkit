package cn.nukkit.network.protocol.types.inventory.creative;

import cn.nukkit.item.Item;

/** One tab/group shown in the Bedrock creative inventory. */
public final class CreativeItemGroup {
    private final int groupId;
    private final CreativeItemCategory category;
    private final String name;
    private final Item icon;

    public CreativeItemGroup(int groupId, CreativeItemCategory category, String name, Item icon) {
        this.groupId = groupId;
        this.category = category;
        this.name = name;
        this.icon = icon;
    }

    public int getGroupId() {
        return groupId;
    }

    public CreativeItemCategory getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public Item getIcon() {
        return icon;
    }
}

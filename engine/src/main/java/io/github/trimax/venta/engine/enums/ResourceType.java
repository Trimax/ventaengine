package io.github.trimax.venta.engine.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResourceType {
    Group(true, "/icons/common/group.png"),
    Folder(true, "/icons/common/folder.png"),
    File(false, "/icons/common/file.png");

    private final boolean isContainer;
    private final String iconPath;
}

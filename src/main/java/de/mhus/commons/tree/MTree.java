package de.mhus.commons.tree;

import de.mhus.commons.errors.MException;
import de.mhus.commons.services.MService;

import java.io.File;
import java.util.List;

public class MTree {

    public static ITreeNode load(File file) throws MException {
        return MService.getService(ITreeNodeFactory.class).read(file);
    }

    public static ITreeNode load(File parent, String name) throws MException {
        return MService.getService(ITreeNodeFactory.class).find(parent, name);
    }

//TODO: implement
//    public static ITreeNode load(String content) throws MException {
//        return MService.getService(ITreeNodeFactory.class).read(content);
//    }

    public static void save(ITreeNode node, File file) throws MException {
        MService.getService(ITreeNodeFactory.class).write(node, file);
    }

    public static ITreeNode create() {
        return MService.getService(ITreeNodeFactory.class).create();
    }

    public static List<String> getArrayValueStringList(TreeNodeList array) {
        return array.stream()
            .map((entry) -> entry.getString(ITreeNode.VALUE).orElse(""))
            .toList();
    }

}

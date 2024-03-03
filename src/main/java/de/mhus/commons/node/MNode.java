package de.mhus.commons.node;

import de.mhus.commons.errors.MException;
import de.mhus.commons.services.MService;

import java.io.File;

public class MNode {

    public static INode load(File file) throws MException {
        return MService.getService(INodeFactory.class).read(file);
    }

    public static INode load(File parent, String name) throws MException {
        return MService.getService(INodeFactory.class).find(parent, name);
    }

}

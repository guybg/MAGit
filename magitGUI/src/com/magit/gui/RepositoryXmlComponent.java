package com.magit.gui;

import com.magit.logic.system.managers.BranchManager;
import com.magit.logic.system.managers.ImportRepositoryTask;
import com.magit.logic.system.managers.RepositoryManager;

public class RepositoryXmlComponent {

    private ImportRepositoryTask task;
    //private XmlImportController controller;


    //public RepositoryXmlComponent(XmlImportController controller) {
        //this.controller = controller;
    //}

    public void importRepositoryFromXml(String filePath, RepositoryManager repositoryManager, BranchManager branchManager, boolean forceCreation) {
        task = new ImportRepositoryTask(filePath, repositoryManager, branchManager, forceCreation);

        //controller.
    }
}

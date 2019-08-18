package com.magit.gui;

import com.magit.controllers.XmlImportController;
import com.magit.logic.system.managers.BranchManager;
import com.magit.logic.system.managers.ImportRepositoryTask;
import com.magit.logic.system.managers.RepositoryManager;

public class RepositoryXmlComponent {

    private ImportRepositoryTask task;
    private XmlImportController controller;


    public RepositoryXmlComponent(XmlImportController controller) {
        this.controller = controller;
    }

    public void importRepositoryFromXml(RepositoryManager repositoryManager, BranchManager branchManager, boolean forceCreation) {
        task = new ImportRepositoryTask(controller.getFilePath(), repositoryManager, branchManager, forceCreation);

        controller.bindTaskToUi(task);

        new Thread(task).start();
    }
}

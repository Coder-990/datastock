package hr.datastock.controllers.service;

import hr.datastock.controllers.LoginController;

import java.io.IOException;

public interface LoginControllerService {
    void close(LoginController loginController);

    void register(LoginController loginController) throws IOException;

    void userLogin(LoginController loginController);

    String getInputDataForDialogCheck(LoginController loginController);
}

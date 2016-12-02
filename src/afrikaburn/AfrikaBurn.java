package afrikaburn;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * @Author:         FN Lombard
 * @Company:        VASTech
 * @Description:    This program runs a GUI to enable the user to do the layout
 *                  for AfrikaBurn
 * 
 * @Notes:          This program requires Java 1.8 to run, since it uses Lambda 
 *                  commands to simplify the code and make it more readable.
 */

public class AfrikaBurn extends Application {

    // Components
    private final String cobraCSS = "Resources/Cobra.css";
    private Stage window;
    
    // Overrides launch method from the Application Class
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Set on close request
        window = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("View.fxml"));
        Parent root = (Parent)loader.load();
        Controller controller = (Controller)loader.getController();
        controller.setWindow(window, root);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
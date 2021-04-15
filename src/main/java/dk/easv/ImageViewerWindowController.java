package dk.easv;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class ImageViewerWindowController implements Initializable {
    public static final List<Image> images = new ArrayList<>();
    public static int currentImageIndex = 0;
    private long showtime;
    private boolean isSlideshowActive;

    @FXML
    private Button slideshowBtn;

    @FXML
    private Slider slideshowSlider;

    @FXML
    Parent root;

    @FXML
    public ImageView imageView;

    private Object lock = new Object();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        isSlideshowActive = true;
        showtime = (long) slideshowSlider.getValue();

        slideshowSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                showtime = (long) slideshowSlider.getValue();
            }
        });

    }

    @FXML
    private void handleBtnLoadAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select image files");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Images",
                "*.png", "*.jpg", "*.gif", "*.tif", "*.bmp"));
        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());

        if (!files.isEmpty()) {
            files.forEach((File f) ->
            {
                images.add(new Image(f.toURI().toString()));
            });
            displayImage();
        }
    }

    @FXML
    public void handleBtnNextAction() {
        displayNextImage();
    }

    @FXML
    private void handleBtnPreviousAction() {
        displayPreviousImage();
    }

    public void displayNextImage() {
        if (!images.isEmpty()) {
            currentImageIndex = (currentImageIndex + 1) % images.size();
            displayImage();
        }
    }

    private void displayPreviousImage() {
        if (!images.isEmpty()) {
            currentImageIndex =
                    (currentImageIndex - 1 + images.size()) % images.size();
            displayImage();
        }
    }

    public void displayImage() {
        if (!images.isEmpty()) {
            imageView.setImage(images.get(currentImageIndex));
            System.out.println(currentImageIndex);
        }
    }

    @FXML
    private void handleBtnSlideshow(ActionEvent actionEvent) {

        isSlideshowActive = !isSlideshowActive;

        if (!images.isEmpty()) {
            Thread thread = new Thread(() -> {
                synchronized (lock) {
                    while(!isSlideshowActive) {
                        System.out.println(Thread.currentThread().getName());
                        Platform.runLater(() -> displayNextImage());
                        try {
                            TimeUnit.SECONDS.sleep(showtime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            thread.setDaemon(true);

            if (!isSlideshowActive) {
                slideshowBtn.setText("Stop slideshow");
                if(!thread.isAlive())
                    thread.start();

            } else {
                slideshowBtn.setText("Start slideshow");
                System.out.println("stop");
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // TODO: FIX MULTIPLE THREADS

        } else {
            System.out.println("Image list is empty!");
        }
    }
}
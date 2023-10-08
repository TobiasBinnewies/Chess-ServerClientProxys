package org.example.ui;

import javafx.scene.image.Image;

import java.util.Objects;

public class Helper {

    public static Image loadImage(String resource, int width, int height) {
        return new Image(Objects.requireNonNull(Helper.class.getClassLoader().getResourceAsStream(resource)), width, height, true, true);
    }
}

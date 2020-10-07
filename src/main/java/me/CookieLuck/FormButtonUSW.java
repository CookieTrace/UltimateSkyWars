package me.CookieLuck;

import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;

public class FormButtonUSW extends ElementButton {
    private String text = "";
    private ElementButtonImageData image;
    String related;

    public FormButtonUSW(String text) {
        super(text);
    }

    public FormButtonUSW(String related, String text, ElementButtonImageData image) {
        super(text, image);
        this.related = related;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ElementButtonImageData getImage() {
        return image;
    }

    public void addImage(ElementButtonImageData image) {
        if (!image.getData().isEmpty() && !image.getType().isEmpty()) this.image = image;
    }
}

package com.gtnewhorizons.navigator.mixins.late.journeymap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.google.common.base.Joiner;
import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import journeymap.client.io.FileHandler;
import journeymap.client.render.texture.TextureCache;

@Mixin(value = TextureCache.class, remap = false)
public abstract class TextureCacheMixin {

    @WrapOperation(
        method = "getThemeTexture(Ljourneymap/client/ui/theme/Theme;Ljava/lang/String;IIZFZ)Ljourneymap/client/render/texture/TextureImpl;",
        at = @At(
            value = "INVOKE",
            target = "Ljourneymap/client/io/FileHandler;getIconFromFile(Ljava/io/File;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;"))
    private BufferedImage navigator$getIconFromResourceLocation(File parentDir, String assetPath, String themeDir,
        String iconPath, BufferedImage defaultImg, Operation<BufferedImage> original) {
        int index = iconPath.indexOf(":");
        if (index == -1) {
            return original.call(parentDir, assetPath, themeDir, iconPath, defaultImg);
        }

        String fixedPath = iconPath.replaceFirst("icon/", "");
        index = fixedPath.indexOf(":");

        String location = "/assets/" + fixedPath.substring(0, index) + "/" + fixedPath.substring(index + 1);
        String icon = "icon/" + fixedPath.substring(fixedPath.lastIndexOf("/") + 1);
        return navigator$getIconFromFile(parentDir, location, themeDir, icon);
    }

    @Unique
    private BufferedImage navigator$getIconFromFile(File parentDir, String assetsPath, String themeDir,
        String iconPath) {
        if (iconPath == null) return null;

        BufferedImage img = null;
        try {
            String filePath = Joiner.on(File.separatorChar)
                .join(themeDir, iconPath.replace('/', File.separatorChar));
            File iconFile = new File(parentDir, filePath);
            if (iconFile.exists()) {
                img = FileHandler.getImage(iconFile);
            }

            if (img == null) {
                InputStream is = NavigatorApi.class.getResourceAsStream(assetsPath);
                if (is == null) {
                    return null;
                } else {
                    img = ImageIO.read(is);
                    is.close();
                }
            }
        } catch (Exception ignored) {}

        return img;
    }
}

package msifeed.mc.mellow.mc;

import cpw.mods.fml.common.FMLCommonHandler;
import msifeed.mc.mellow.handlers.KeyHandler;
import msifeed.mc.mellow.handlers.MouseHandler;
import msifeed.mc.mellow.render.RenderUtils;
import msifeed.mc.mellow.utils.Point;
import msifeed.mc.mellow.widgets.Widget;
import msifeed.mc.mellow.widgets.scene.ProfilingScene;
import msifeed.mc.mellow.widgets.scene.Scene;
import msifeed.mc.mellow.widgets.window.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class MellowGuiScreen extends GuiScreen {
    private static final Logger LOGGER = LogManager.getLogger("Mellow.GuiScreen");
    protected Scene scene = new ProfilingScene();

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        final Minecraft mc = Minecraft.getMinecraft();
        final int scaleFactor = RenderUtils.getScreenScaleFactor();
        scene.getGeometry().set(0, 0, mc.displayWidth / scaleFactor, mc.displayHeight / scaleFactor);
    }

    @Override
    public void drawScreen(int xMouse, int yMouse, float tick) {
        try {
            scene.update();
            scene.render();
            Widget.hoveredWidget = scene.lookupWidget(new Point(xMouse, yMouse)).orElse(null);
        } catch (Exception e) {
            LOGGER.error(e);
            Minecraft.getMinecraft().displayGuiScreen(null);
            return;
        }

        if (scene.getChildren().isEmpty()) {
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
    }

    @Override
    protected void mouseClicked(int xMouse, int yMouse, int button) {
        try {
            final Widget lookup = scene.lookupWidget(new Point(xMouse, yMouse)).orElse(null);
            if (lookup instanceof MouseHandler.Press)
                ((MouseHandler.Press) lookup).onPress(xMouse, yMouse, button);
            Widget.setFocused(lookup);
            Widget.pressedWidget = lookup;
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    @Override
    protected void mouseClickMove(int xMouse, int yMouse, int button, long timeSinceMouseClick) {
        if (Widget.pressedWidget instanceof MouseHandler.Move)
            ((MouseHandler.Move) Widget.pressedWidget).onMove(xMouse, yMouse, button);
    }

    @Override
    protected void mouseMovedOrUp(int xMouse, int yMouse, int button) {
        if (Widget.pressedWidget == null)
            return;

        try {
            final Widget widget = Widget.pressedWidget;
            if (button == -1) {
                if (widget instanceof MouseHandler.Move)
                    ((MouseHandler.Move) widget).onMove(xMouse, yMouse, button);
            } else {
                if (widget instanceof MouseHandler.Release)
                    ((MouseHandler.Release) widget).onRelease(xMouse, yMouse, button);

                // If move mouse away click don't counts
                final Widget lookup = scene.lookupWidget(new Point(xMouse, yMouse)).orElse(null);
                if (lookup == Widget.pressedWidget) {
                    if (Widget.pressedWidget == widget && widget instanceof MouseHandler.Click)
                        ((MouseHandler.Click) widget).onClick(xMouse, yMouse, button);
                }

                Widget.pressedWidget = null;
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    @Override
    public void handleKeyboardInput() {
        Keyboard.enableRepeatEvents(true);
        if (!Keyboard.getEventKeyState())
            return;

        final char c = Keyboard.getEventCharacter();
        final int k = Keyboard.getEventKey();

        if (k == Keyboard.KEY_ESCAPE) {
            Widget focusedWindow = Widget.focusedWidget;

            if (focusedWindow != null) {
                while (focusedWindow != null && !(focusedWindow instanceof Window))
                    focusedWindow = focusedWindow.getParent();
            } else {
                for (Widget w : scene.getChildren()) {
                    if (w instanceof Window) {
                        focusedWindow = w;
                        break;
                    }
                }
            }

            if (focusedWindow != null) {
                focusedWindow.getParent().removeChild(focusedWindow);
                Widget.setFocused(null);
            }
            return;
        }

        if (Widget.focusedWidget instanceof KeyHandler)
            ((KeyHandler) Widget.focusedWidget).onKeyboard(c, k);
        else {
            FMLCommonHandler.instance().fireKeyInput();
        }
    }
}

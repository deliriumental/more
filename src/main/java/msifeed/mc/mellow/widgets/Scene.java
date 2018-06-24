package msifeed.mc.mellow.widgets;

import msifeed.mc.mellow.layout.AnchorLayout;
import msifeed.mc.mellow.utils.Point;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.profiler.Profiler;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Optional;

public class Scene extends Widget {

    public Scene() {
        setLayout(new AnchorLayout(AnchorLayout.Anchor.CENTER));
    }

    @Override
    public void update() {
        final Profiler pr = Minecraft.getMinecraft().mcProfiler;
        pr.startSection("MellowUpdate");
        super.update();
        pr.endSection();
    }

    @Override
    public void render() {
        final Profiler pr = Minecraft.getMinecraft().mcProfiler;
        pr.startSection("MellowRender");
        super.render();
        pr.endSection();
    }

    public Optional<Widget> lookupWidget(Point p) {
        final Profiler pr = Minecraft.getMinecraft().mcProfiler;
        pr.startSection("MellowLookup");
        final Optional<Widget> lookup = fullLookup(p);
        pr.endSection();

        return lookup;
    }

    private Optional<Widget> fullLookup(Point p) {
        ArrayList<Widget> active = new ArrayList<>();
        ArrayList<Widget> pending = new ArrayList<>(getLookupChildren());
        ArrayList<Widget> nextPending = new ArrayList<>();

        while (!pending.isEmpty()) {
            for (Widget pw : pending) {
                for (Widget w : pw.getLookupChildren()) {
                    if (w.isVisible())
                        nextPending.add(w);
                }
            }
            active.addAll(pending);
            pending.clear();
            pending.addAll(nextPending);
            nextPending.clear();
        }

//        GL11.glPushMatrix();
//        GL11.glScalef(0.5f, 0.5f, 0.5f);
//        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
//        int y = 5;
//        for (Object o : active.stream().filter(widget -> widget.containsPoint(p)).toArray()) {
//            fr.drawString(o.getClass().getSimpleName(), 5, y, 0xffffff);
//            y += fr.FONT_HEIGHT + 2;
//        }
//        GL11.glPopMatrix();

        return active.isEmpty()
                ? Optional.empty()
                : Optional.ofNullable(active
                .stream()
                .filter(widget -> widget.containsPoint(p))
                .reduce(active.get(0), (top, w) -> w.isHigherThan(top) ? w : top));
    }
}

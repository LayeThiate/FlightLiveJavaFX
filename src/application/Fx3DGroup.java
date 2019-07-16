package application;

import java.util.Collection;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
 
public class Fx3DGroup extends Group {
 

        
    public Translate t  = new Translate();
    public Rotate rx = new Rotate();
    { rx.setAxis(Rotate.X_AXIS); }
    public Rotate ry = new Rotate();
    { ry.setAxis(Rotate.Y_AXIS); }
    public Rotate rz = new Rotate();
    { rz.setAxis(Rotate.Z_AXIS); }
    public Scale s = new Scale();

    public Fx3DGroup() {
        super();
        init();
    }
    
    public Fx3DGroup(Node... children) {
        super(children);
        init();
    }
    
    public Fx3DGroup(Collection<Node> children) {
        super(children);
        init();
    }
    
    private void init() {
        getTransforms().addAll(t, rz, ry, rx, s);
    }
    

    public void set3DTranslate(double x, double y, double z) {
        t.setX(x);
        t.setY(y);
        t.setZ(z);
    }



    public void set3DRotate(double x, double y, double z) {
        rx.setAngle(x);
        ry.setAngle(y);
        rz.setAngle(z);
    }


    public void set3DScale(double scaleFactor) {
        s.setX(scaleFactor);
        s.setY(scaleFactor);
        s.setZ(scaleFactor);
    }

    public void set3DScale(double scaleFactorX, double scaleFactorY, double scaleFactorZ) {
        s.setX(scaleFactorX);
        s.setY(scaleFactorY);
        s.setZ(scaleFactorZ);
    }



    public void reset() {
        t.setX(0.0);
        t.setY(0.0);
        t.setZ(0.0);
        rx.setAngle(0.0);
        ry.setAngle(0.0);
        rz.setAngle(0.0);
        s.setX(1.0);
        s.setY(1.0);
        s.setZ(1.0);
    }

    public void debug() {
        System.out.println("t = (" +
                           t.getX() + ", " +
                           t.getY() + ", " +
                           t.getZ() + ")  " +
                           "r = (" +
                           rx.getAngle() + ", " +
                           ry.getAngle() + ", " +
                           rz.getAngle() + ")  " +
                           "s = (" +
                           s.getX() + ", " +
                           s.getY() + ", " +
                           s.getZ() + ")  ");
    }
}

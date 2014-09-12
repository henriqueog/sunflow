package org.sunflow.core.shader;

import org.sunflow.SunflowAPI;
import org.sunflow.core.ParameterList;
import org.sunflow.core.Ray;
import org.sunflow.core.Shader;
import org.sunflow.core.ShadingState;
import org.sunflow.image.Color;
import org.sunflow.math.Vector3;
import org.sunflow.math.Point3;
import org.sunflow.math.OrthoNormalBasis;

public class TranslucentShader implements Shader {
//allocation of basic values
    // object color
    public Color color = Color.WHITE;
// object absorption color
//public Color absorptionColor = Color.RED;
    public Color absorptionColor = Color.BLUE;
// inverse of absorption color
    public Color transmittanceColor = absorptionColor.copy().opposite();
// global color-saving variable
    /* FIXME!?? - globals are not good */
    public Color glob = Color.black();
// phong specular color
    public Color pcolor = Color.BLACK;
// object absorption distance
    public float absorptionDistance = 0.25f;
// depth correction parameter
    public float thickness = 0.002f;
// phong specular power
    public float ppower = 85f;
// phong specular samples
    public int psamples = 1;
// phong flag
    public boolean phong = false;

    public boolean update(ParameterList pl, SunflowAPI api) {
        color = pl.getColor("color", color);
        absorptionColor=pl.getColor("absorptionColor",absorptionColor)
        glob=pl.getColor("glob",glob)
        pcolor=pl.getColor("pcolor",pcolor)
        absorptionDistance=pl.getFloat("absorptionDistance",absorptionDistance)
        thickness=pl.getFloat("thicknessa",thickness)
        ppower=pl.getfloat("ppower",ppower)
        psamples=pl.getInt("psamples",psamples)
        
        if (absorptionDistance == 0f) {
            absorptionDistance+= 0.0000001f;
        }
        if (!pcolor.isBlack()) {
            phong = true;
        }
        return true;
    }
    public Color getRadiance(ShadingState state) {
        Color ret = Color.black();
        Color absorbtion = Color.white();
        glob.set(Color.black());
        state.faceforward();
        state.initLightSamples();
        state.initCausticSamples();
        if (state.getRefractionDepth() == 0) {
            ret.set(state.diffuse(color).mul(0.5f));
            bury(state,thickness);
        } else {
            absorbtion = Color.mul(-state.getRay().getMax() / absorptionDistance, transmittanceColor).exp();
        }
        state.traceRefraction(new Ray(state.getPoint(), randomVector()), 0);
        glob.add(state.diffuse(color));
        glob.mul(absorbtion);
        if (state.getRefractionDepth() == 0 && phong) {
            bury(state,-thickness);
            glob.add(state.specularPhong(pcolor,ppower,psamples));
        }
        return glob;
    }
    public void bury(ShadingState state, float th) {
        Point3 pt = state.getPoint();
        Vector3 norm = state.getNormal();
        pt.x = pt.x - norm.x * th;
        pt.y = pt.y - norm.y * th;
        pt.z = pt.z - norm.z * th;
    }
    public Vector3 randomVector() {
        return new Vector3(
            (float)(2f*Math.random()-1f),
            (float)(2f*Math.random()-1f),
            (float)(2f*Math.random()-1f)
            ).normalize();
    }
    public Color getDiffuse(ShadingState state) {
        return color;
    }
    public void scatterPhoton(ShadingState state, Color power) {
        Color diffuse = getDiffuse(state);
        state.storePhoton(state.getRay().getDirection(), power, diffuse);
        state.traceReflectionPhoton(new Ray(state.getPoint(), randomVector()), power.mul(diffuse));
    }
}

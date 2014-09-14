package org.sunflow.core.shader;

import org.sunflow.SunflowAPI;
import org.sunflow.core.ParameterList;
import org.sunflow.core.Ray;
import org.sunflow.core.Shader;
import org.sunflow.core.ShadingState;
import org.sunflow.image.Color;
import org.sunflow.math.OrthoNormalBasis;
import org.sunflow.math.Vector3;
import org.sunflow.math.Point3;

public class SimpleSSS implements Shader {
    Color diff = new Color(0.3f,1.0f,0.6f);
    Color spec = new Color(1.0f,1.0f,1.0f);
    Color bright = new Color(1f,1f,1f);
    Color dark = new Color(0f,0f,0f);
    Color color = new Color(0.6f,0.8f,0.8f);
    Color absorbtionColor = new Color(0.6f,0.5f,0.3f).opposite();
    int numRays = 5;
    float power = 100;
    float reflectiveness = 0.8f;
    float hardness = 0.15f;
    float depth = 0.15f;
    float spread = 1f;
    float glossyness = 0.8f;
    float absorbtionValue = 0.5f;

    public boolean update(ParameterList pl, SunflowAPI api) {
        return true;
    }
    protected Color getDiffuse(ShadingState state) {
        return diff;
    }
    public Color getRadiance(ShadingState state) {
        state.faceforward();
        state.initLightSamples();
        state.initCausticSamples();
// execute shader
        float cos = state.getCosND();
        float dn = 2 * cos;
        Vector3 refDir = new Vector3();
        refDir.x = (dn * state.getNormal().x) + state.getRay().getDirection().x;
        refDir.y = (dn * state.getNormal().y) + state.getRay().getDirection().y;
        refDir.z = (dn * state.getNormal().z) + state.getRay().getDirection().z;
        Ray refRay = new Ray(state.getPoint(), refDir);
        Color reflections = Color.WHITE;
        Color highlights = Color.WHITE;
        reflections = state.traceReflection(refRay, 0);
        highlights = state.diffuse(getDiffuse(state)).add(state.specularPhong(spec, power, numRays));
        reflections = Color.blend(highlights,reflections,reflectiveness);
        Color sColor = state.diffuse(getDiffuse(state));
        sColor = Color.blend(sColor,reflections,glossyness);
        Vector3 norm = state.getNormal();
        Point3 pt = state.getPoint();
        pt.x += norm.x*spread;
        pt.y += norm.y*spread;
        pt.z += norm.z*spread;
        state.getPoint().set(pt);
        Color tColor = state.getIrradiance(sColor);
        pt.x -= norm.x*(spread+depth);
        pt.y -= norm.y*(spread+depth);
        pt.z -= norm.z*(spread+depth);
        state.getPoint().set(pt);
        state.getNormal().set(state.getRay().getDirection());
        Color sssColor = Color.add(diff,tColor.mul(state.occlusion(16,absorbtionValue, bright,
            dark).opposite().mul(absorbtionColor)));
        sssColor.mul(absorbtionColor);
        return Color.blend(sssColor,sColor,hardness);
    }
    public void scatterPhoton(ShadingState state, Color power) {
//just a copy of the scatter method in PhongShader.......
// make sure we are on the right side of the material
        state.faceforward();
        Color d = getDiffuse(state);
        state.storePhoton(state.getRay().getDirection(), power, d);
        float avgD = d.getAverage();
        float avgS = spec.getAverage();
        double rnd = state.getRandom(0, 0, 1);
        if (rnd < avgD) {
// photon is scattered diffusely
            power.mul(d).mul(1.0f / avgD);
            OrthoNormalBasis onb = state.getBasis();
            double u = 2 * Math.PI * rnd / avgD;
            double v = state.getRandom(0, 1, 1);
            float s = (float) Math.sqrt(v);
            float s1 = (float) Math.sqrt(1.0f - v);
            Vector3 w = new Vector3((float) Math.cos(u) * s, (float) Math.sin(u) * s, s1);
            w = onb.transform(w, new Vector3());
            state.traceDiffusePhoton(new Ray(state.getPoint(), w), power);
        }
        else if (rnd < avgD + avgS) {
// photon is scattered specularly
            float dn = 2.0f * state.getCosND();
// reflected direction
            Vector3 refDir = new Vector3();
            refDir.x = (dn * state.getNormal().x) + state.getRay().dx;
            refDir.y = (dn * state.getNormal().y) + state.getRay().dy;
            refDir.z = (dn * state.getNormal().z) + state.getRay().dz;
            power.mul(spec).mul(1.0f / avgS);
            OrthoNormalBasis onb = state.getBasis();
            double u = 2 * Math.PI * (rnd - avgD) / avgS;
            double v = state.getRandom(0, 1, 1);
            float s = (float) Math.pow(v, 1 / (this.power + 1));
            float s1 = (float) Math.sqrt(1 - s * s);
            Vector3 w = new Vector3((float) Math.cos(u) * s1, (float) Math.sin(u) * s1, s);
            w = onb.transform(w, new Vector3());
            state.traceReflectionPhoton(new Ray(state.getPoint(), w), power);
        }
    }
    public void init(ShadingState state) {
    }
}
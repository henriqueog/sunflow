package org.sunflow.core.shader;
 
import org.sunflow.core.ShadingState;
import org.sunflow.image.Color;
import org.sunflow.core.ParameterList;
import org.sunflow.SunflowAPI;
import org.sunflow.core.shader.*;
public class FresnelShader implements Shader {
 
public Color edgeColor;
public Color surfaceColor;
private DiffuseShader light;
private DiffuseShader dark;
private Int samples;
 
public FresnelShader()
{
  edgeColor = Color.WHITE;
  surfaceColor = Color.GRAY;
  light = new DiffuseShader();
  dark = new DiffuseShader();
}
 
  public boolean update(ParameterList pl, SunflowAPI api) {
        edgeColor = pl.getColor("edge_color", edgeColor);
        surfaceColor = pl.getColor("surface_color", surfaceColor);
        return true;
  }
 
 public Color getRadiance(ShadingState state) {
  //olhar para a face correta
  state.faceforward();
  state.initLightSamples();
   
  float c=state.getCosND();
 
  //original
  //Color l = new Color( light.getRadiance(state) );
  //Color d = new Color( dark.getRadiance(state) );
   
  Color l = new Color(edgeColor);
  Color d = new Color(surfaceColor);
 
  return Color.blend(l, d, c);
 }
 
 public void scatterPhoton(ShadingState state, Color power) {
     light.scatterPhoton(getDiffuse(state), power); //do we need to do this?
     dark.scatterPhoton(getDiffuse(state), power);
    }
 
    public Color getDiffuse(ShadingState state) {
     pl.addColor( "diffuse", surfaceColor);
     boolean b2 = dark.update(pl, api);
      
     pl.addColor( "diffuse", edgeColor);
     boolean b1 = light.update(pl, api);
      
     return (b1 && b2);
    }
   }

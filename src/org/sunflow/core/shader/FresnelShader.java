package org.sunflow.core.shader;

import org.sunflow.core.ShadingState;
import org.sunflow.image.Color;
import org.sunflow.core.ParameterList;
import org.sunflow.SunflowAPI;
import org.sunflow.core.shader.*;
public class FresnelShader implements Shader {

public Color edgeColor;
public Color surfaceColor;
private DiffuseShader light=new DiffuseShader();
private DiffuseShader dark=new DiffuseShader();

 public Color getRadiance(ShadingState state) {
  //olhar para a face correta
  state.faceforward();

  float c=state.getCosND();

  Color l = new Color( light.getRadiance(state) );

  Color d = new Color( dark.getRadiance(state) );

  return Color.blend(l, d, c);
 }

 public void scatterPhoton(ShadingState state, Color power) {
     light.scatterPhoton(state, power); //do we need to do this?
    }

    public boolean update(ParameterList pl, SunflowAPI api) {
     pl.addColor( "diffuse", new Color(1.0f,0.0f,0.0f) );
     boolean b2 = dark.update(pl, api);
     pl.addColor( "diffuse", new Color(0.0f,1.0f,0.0f) );
     boolean b1 = light.update(pl, api);
     return (b1 && b2);
    }
   }

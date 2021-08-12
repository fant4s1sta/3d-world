
out vec4 outputColor;

uniform vec4 input_color;

uniform mat4 view_matrix;
uniform mat4 model_matrix;

// Mode
uniform int mode;

// Light properties
uniform vec3 directionalLight;
uniform vec3 lightIntensity;
uniform vec3 ambientIntensityNight;
uniform vec3 ambientIntensityTorch;
uniform vec3 ambientIntensityDay;
uniform vec3 torchLight;

// Material properties
uniform vec3 ambientCoeff;
uniform vec3 diffuseCoeffNight;
uniform vec3 diffuseCoeffTorch;
uniform vec3 diffuseCoeffDay;
uniform vec3 specularCoeffNight;
uniform vec3 specularCoeffTorch;
uniform vec3 specularCoeffDay;
uniform float phongExp;
uniform float cutoff;
uniform float constant;
uniform float linear;
uniform float quadratic;

uniform sampler2D tex;

in vec4 viewPosition;
in vec3 m;

in vec2 texCoordFrag;

void main()
{
    // Day mode
	if (mode == 0) {
		// Compute the s, v and r vectors
		vec3 s = normalize(view_matrix*vec4(directionalLight, 0)).xyz;
		vec3 v = normalize(-viewPosition.xyz);
		vec3 r = normalize(reflect(-s,m));

		vec3 ambient = ambientIntensityDay*ambientCoeff;
		vec3 diffuse = max(lightIntensity*diffuseCoeffDay*dot(m,s), 0.0);
		vec3 specular;

		// Only show specular reflections for the front face
		if (dot(m,s) > 0)
			specular = max(lightIntensity*specularCoeffDay*pow(dot(r,v),phongExp), 0.0);
		else
			specular = vec3(0);

		vec4 ambientAndDiffuse = vec4(ambient + diffuse, 1);

		outputColor = ambientAndDiffuse*input_color*texture(tex, texCoordFrag) + vec4(specular, 1);
	}

    // Night Mode
	if (mode == 1) {
		// Compute the s, v and r vectors
		vec3 s = normalize(view_matrix*vec4(directionalLight, 0)).xyz;
		vec3 v = normalize(-viewPosition.xyz);
		vec3 r = normalize(reflect(-s,m));

		vec3 ambient = ambientIntensityNight*ambientCoeff;
		vec3 diffuse = max(lightIntensity*diffuseCoeffNight*dot(m,s), 0.0);
		vec3 specular;

		// Only show specular reflections for the front face
		if (dot(m,s) > 0)
			specular = max(lightIntensity*specularCoeffNight*pow(dot(r,v),phongExp), 0.0);
		else
			specular = vec3(0);

		vec4 ambientAndDiffuse = vec4(ambient + diffuse, 1);

		outputColor = ambientAndDiffuse*input_color*texture(tex, texCoordFrag) + vec4(specular, 1);
	}

    // Torch Mode
	if (mode == 2) {
        // Compute the s, v and n vectors
		vec3 s = (-vec4(torchLight, 0) + viewPosition).xyz;
		vec3 v = normalize(-viewPosition.xyz);
		vec3 n = normalize(vec3(0, 0, -1));
        
        // Compute the distance between the view point and the fragment
        float d = length(s);
        
        // Compute the attenuation
        float attenuation = 1.0 / (constant + linear * d + quadratic * (d * d));
		
        // Compute the angle
        s = normalize(s);
        float angle = degrees(acos(dot(n, s)));
        
        // Check the cutoff
		if (angle <= cutoff) {
            // Compute the output color for the torch light
            float spotlightAttenuation = pow(cos(radians(angle)), 16.0f);
			vec3 r = normalize(reflect(-s,m));
			vec3 ambient = ambientIntensityTorch*ambientCoeff*attenuation;
			vec3 diffuse = max(lightIntensity*diffuseCoeffTorch*dot(m,s), 0.0)*attenuation;
			vec3 specular;
			if (dot(m,s) > 0)
				specular = max(lightIntensity*specularCoeffTorch*pow(dot(r,v),phongExp), 0.0)*attenuation;
			else
				specular = vec3(0);
			vec4 ambientAndDiffuse = vec4(ambient + diffuse, 1);
			vec4 outputColorTorch = ambientAndDiffuse*input_color*texture(tex, texCoordFrag) + vec4(specular, 1);
            outputColorTorch = spotlightAttenuation*outputColorTorch;
            
            // Compute the output color for the directional light
            s = normalize(view_matrix*vec4(directionalLight, 0)).xyz;
            v = normalize(-viewPosition.xyz);
            r = normalize(reflect(-s,m));
            ambient = ambientIntensityNight*ambientCoeff;
            diffuse = max(lightIntensity*diffuseCoeffNight*dot(m,s), 0.0);
            if (dot(m,s) > 0)
                specular = max(lightIntensity*specularCoeffNight*pow(dot(r,v),phongExp), 0.0);
            else
                specular = vec3(0);
            ambientAndDiffuse = vec4(ambient + diffuse, 1);
            vec4 outputColorLight = ambientAndDiffuse*input_color*texture(tex, texCoordFrag) + vec4(specular, 1);
            
            // Combine two lights
            outputColor = outputColorTorch + outputColorLight;
		} else {
            // Everything outside the cutoff angle should only be lit by the directional light
			s = normalize(view_matrix*vec4(directionalLight, 0)).xyz;
			v = normalize(-viewPosition.xyz);
			vec3 r = normalize(reflect(-s,m));
			vec3 ambient = ambientIntensityNight*ambientCoeff;
			vec3 diffuse = max(lightIntensity*diffuseCoeffNight*dot(m,s), 0.0);
			vec3 specular;
			if (dot(m,s) > 0)
				specular = max(lightIntensity*specularCoeffNight*pow(dot(r,v),phongExp), 0.0);
			else
				specular = vec3(0);
			vec4 ambientAndDiffuse = vec4(ambient + diffuse, 1);
			outputColor = ambientAndDiffuse*input_color*texture(tex, texCoordFrag) + vec4(specular, 1);
		}
	}
}


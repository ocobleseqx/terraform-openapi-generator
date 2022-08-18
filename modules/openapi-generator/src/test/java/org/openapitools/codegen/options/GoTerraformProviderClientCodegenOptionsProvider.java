package org.openapitools.codegen.options;

import org.openapitools.codegen.CodegenConstants;
import org.openapitools.codegen.languages.GoTerraformProviderClientCodegen;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class GoTerraformProviderClientCodegenOptionsProvider implements OptionsProvider {
    public static final String PACKAGE_NAME_VALUE = "OpenApi";

    @Override
    public String getLanguage() {
        return "go-terraform-provider";
    }

    @Override
    public Map<String, String> createOptions() {
        ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<String, String>();
        return builder
                .put(CodegenConstants.PACKAGE_NAME, PACKAGE_NAME_VALUE)
                .build();
    }

    @Override
    public boolean isServer() {
        return false;
    }
}


package org.openapitools.codegen.go.terraform.provider;

import org.openapitools.codegen.AbstractOptionsTest;
import org.openapitools.codegen.CodegenConfig;
import org.openapitools.codegen.languages.GoTerraformProviderClientCodegen;
import org.openapitools.codegen.options.GoTerraformProviderClientCodegenOptionsProvider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class GoTerraformProviderClientCodegenOptionsTest extends AbstractOptionsTest {
    private GoTerraformProviderClientCodegen codegen = mock(GoTerraformProviderClientCodegen.class, mockSettings);

    public GoTerraformProviderClientCodegenOptionsTest() {
        super(new GoTerraformProviderClientCodegenOptionsProvider());
    }

    @Override
    protected CodegenConfig getCodegenConfig() {
        return codegen;
    }

    @SuppressWarnings("unused")
    @Override
    protected void verifyOptions() {
        // TODO: Complete options using Mockito
        // verify(codegen).someMethod(arguments)
    }
}


package org.openapitools.codegen.languages;

import org.openapitools.codegen.*;
import org.openapitools.codegen.meta.features.DocumentationFeature;
import org.openapitools.codegen.meta.GeneratorMetadata;
import org.openapitools.codegen.meta.Stability;
import org.openapitools.codegen.model.ApiInfoMap;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.parameters.Parameter;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoTerraformProviderClientCodegen extends AbstractGoCodegen implements CodegenConfig {
    // public static final String PROJECT_NAME = "projectName";

    static final Logger LOGGER = LoggerFactory.getLogger(GoTerraformProviderClientCodegen.class);
    protected String packageVersion = "1.0.0";
    protected String apiDocPath = "docs/";
    protected String modelDocPath = "docs/";
    public static final String WITH_XML = "withXml";
    public static final String STRUCT_PREFIX = "structPrefix";
    public static final String WITH_AWSV4_SIGNATURE = "withAWSV4Signature";
    public static final String GENERATE_INTERFACES = "generateInterfaces";
    protected String goImportAlias = "openapiclient";
    protected boolean isGoSubmodule = false;
    protected boolean useOneOfDiscriminatorLookup = false; // use oneOf discriminator's mapping for model lookup

    // // A cache to efficiently lookup schema `toModelName()` based on the schema Key
    // private Map<String, String> schemaKeyToApiNameCache = new HashMap<>();

    /**
     * Configures the type of generator.
     *
     * @return the CodegenType for this generator
     * @see org.openapitools.codegen.CodegenType
     */
    @Override
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    /**
     * Configures a friendly name for the generator. This will be used by the
     * generator to select the library with the -g flag.
     *
     * @return the friendly name for the generator
     */
    @Override
    public String getName() {
        return "go-terraform-provider";
    }

    /**
     * Returns human-friendly help for the generator. Provide the consumer with help
     * tips, parameters here
     *
     * @return A string value for the help message
     */
    @Override
    public String getHelp() {
        return "Generates a go-terraform-provider client.";
    }

    @Override
    public String toGetter(String name) {
        return "Get" + getterAndSetterCapitalize(name);
    }

    public GoTerraformProviderClientCodegen() {
        super();

        modifyFeatureSet(features -> features
                .includeDocumentationFeatures(
                    DocumentationFeature.Readme,
                    DocumentationFeature.Api
                )
                // .wireFormatFeatures(EnumSet.of(WireFormatFeature.JSON))
                // .securityFeatures(EnumSet.of(
                //         SecurityFeature.BasicAuth,
                //         SecurityFeature.ApiKey,
                //         SecurityFeature.OAuth2_Implicit
                // ))
                // .includeGlobalFeatures(
                //         GlobalFeature.ParameterizedServer
                // )
                // .excludeGlobalFeatures(
                //         GlobalFeature.XMLStructureDefinitions,
                //         GlobalFeature.Callbacks,
                //         GlobalFeature.LinkObjects,
                //         GlobalFeature.ParameterStyling
                // )
                // .excludeSchemaSupportFeatures(
                //         SchemaSupportFeature.Polymorphism
                // )
                // .includeParameterFeatures(
                //         ParameterFeature.Cookie
                // )
                // .includeClientModificationFeatures(
                //         ClientModificationFeature.BasePath,
                //         ClientModificationFeature.UserAgent
                // )
        );

        generatorMetadata = GeneratorMetadata.newBuilder(generatorMetadata).stability(Stability.EXPERIMENTAL).build();

        outputFolder = "generated-code" + File.separator + "go-terraform-provider";
        embeddedTemplateDir = templateDir = "go-terraform-provider";
        usesOptionals = false;

        apiPackage = "Test";
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        // TODO: Fill this out.
    }

    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        super.postProcessSupportingFileData(objs);

        ApiInfoMap apiInfo = (ApiInfoMap) objs.get("apiInfo");
        List<CodegenOperation> ops = apiInfo.getApis().stream()
                .flatMap(api -> api.getOperations().getOperation().stream())
                .collect(Collectors.toList());
        ops.forEach(op -> {
            if ("POST".equalsIgnoreCase(op.httpMethod)) {
                if (op.vendorExtensions.containsKey("x-terraform")) {
                    Object tf = op.vendorExtensions.get("x-terraform");
                    Map<String, String> tfMap = (HashMap<String, String>) tf;
                    supportingFiles.add(new SupportingFile("resource.mustache", "", String.format("resource_%s_%s.go", tfMap.get("service"), tfMap.get("resource"))));
                }
            }
        
        });

        return objs;
    }
}

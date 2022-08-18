package org.openapitools.codegen.languages;

// import com.samskivert.mustache.Mustache;
// import com.samskivert.mustache.Template;
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
    // protected String apiDocPath = "docs/";
    // protected String modelDocPath = "docs/";
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

        cliOptions.clear();
        cliOptions.add(new CliOption(CodegenConstants.PACKAGE_NAME, "Terraform provider name (convention: lowercase).")
                .defaultValue(this.packageName));
    }

    @Override
    public void processOpts() {
        super.processOpts();

        // TODO add additional lambda functions
        // additionalProperties.put("lambda",new Mustache.Lambda() {
        //     @Override
        //     public void execute(Template.Fragment fragment, Writer writer) throws IOException {
        //         writer.write(length(fragment.context()));
        //     }
        // });

        if (additionalProperties.containsKey(CodegenConstants.PACKAGE_NAME)) {
            setPackageName((String) additionalProperties.get(CodegenConstants.PACKAGE_NAME));
        } else {
            setPackageName("openapi");
        }
        additionalProperties.put(CodegenConstants.PACKAGE_NAME, packageName);
        // additionalProperties.put("apiDocPath", apiDocPath);
        apiPackage = packageName;

        // add lambda for mustache templates to handle oneOf/anyOf naming
        // e.g. []string => ArrayOfString
        // additionalProperties.put("lambda.type-to-name", (Mustache.Lambda) (fragment, writer) -> writer.write(typeToName(fragment.execute())));

        //mustache templates
        supportingFiles.add(new SupportingFile("gitignore.mustache", "", ".gitignore"));
        supportingFiles.add(new SupportingFile("main.mustache", "", "main.go"));
        supportingFiles.add(new SupportingFile("openapi.mustache", "api", "openapi.yaml"));
        supportingFiles.add(new SupportingFile("git_push.sh.mustache", "", "git_push.sh"));
        supportingFiles.add(new SupportingFile("go.mod.mustache", "", "go.mod"));
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("provider/errors.mustache", apiPackage, "errors.go"));
        supportingFiles.add(new SupportingFile("provider/utils.mustache", apiPackage, "utils.go"));
        supportingFiles.add(new SupportingFile("provider/provider.mustache", apiPackage, "provider.go"));
        supportingFiles.add(new SupportingFile("provider/provider_test.mustache", apiPackage, "provider_test.go"));


        //base files
        supportingFiles.add(new SupportingFile(".goreleaser.yml", "", ".goreleaser.yml"));
        supportingFiles.add(new SupportingFile("CHANGELOG.md", "", "CHANGELOG.md"));
        supportingFiles.add(new SupportingFile("go.sum", "", "go.sum"));
        supportingFiles.add(new SupportingFile(".travis.yml", "", ".travis.yml"));
        supportingFiles.add(new SupportingFile("terraform-registry-manifest.json", "", "terraform-registry-manifest.json"));
    }

    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        super.postProcessSupportingFileData(objs);

        ApiInfoMap apiInfo = (ApiInfoMap) objs.get("apiInfo");
        List<CodegenOperation> ops = apiInfo.getApis().stream()
                .flatMap(api -> api.getOperations().getOperation().stream())
                .collect(Collectors.toList());
        ops.forEach(op -> {
            if (op.vendorExtensions.containsKey("x-terraform")) {
                Object tf = op.vendorExtensions.get("x-terraform");
                Map<String, Object> tfMap = (HashMap<String, Object>) tf;

                if (tfMap.containsKey("excludeResource") && Boolean.valueOf(tfMap.get("excludeResource").toString())) return;

                String svc = String.valueOf(tfMap.get("service"));
                String res = String.valueOf(tfMap.get("resource")); // API Object --> e.g. connection
                Boolean isDataSource = false;
                if (tfMap.containsKey("isReadFuncAndDataSource")){
                    isDataSource = Boolean.valueOf(tfMap.get("isReadFuncAndDataSource").toString());
                }

                if ("POST".equalsIgnoreCase(op.httpMethod)) {
                    supportingFiles.add(new SupportingFile("provider/resource.mustache", this.apiPackage , String.format("resource_%s_%s.go", svc, res)));
                    supportingFiles.add(new SupportingFile("provider/resource_test.mustache", this.apiPackage, String.format("resource_%s_%s_test.go", svc, res)));
                    supportingFiles.add(new SupportingFile("provider/resource_schema.mustache", this.apiPackage, String.format("%s_%s_schema.go", svc, res)));
                }

                if (isDataSource) {
                    supportingFiles.add(new SupportingFile("provider/datasource.mustache", this.apiPackage, String.format("data_source_%s_%s.go", svc, res)));
                    supportingFiles.add(new SupportingFile("provider/datasource_test.mustache", this.apiPackage, String.format("data_source_%s_%s.go", svc, res)));
                    supportingFiles.add(new SupportingFile("provider/datasource_schema.mustache", this.apiPackage, String.format("%s_%s_read_schema.go", svc, res)));
                }
            }
        });

        return objs;
    }
}

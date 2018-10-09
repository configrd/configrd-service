package com.appcrossings.config;

import java.util.HashMap;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import com.appcrossings.config.file.FileRepoDef;

public class RepoConfigBuilder {

  Yaml repoConfig;
  final Map<String, Map<String, Object>> config = new HashMap<>();
  final Map<String, Object> repos = new HashMap<>();

  public RepoConfigBuilder() {
    
    PropertyUtils propUtils = new PropertyUtils();
    propUtils.setAllowReadOnlyProperties(true);
    propUtils.setSkipMissingProperties(true);
    propUtils.setBeanAccess(BeanAccess.PROPERTY);
    Representer repr = new Representer() {
      
      @Override
      protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue,Tag customTag) {
          // if value of property is null, ignore it.
          if (propertyValue == null) {
              return null;
          }  
          else {
              return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
          }
      }
      
    };
 
    repr.setPropertyUtils(propUtils);
    repoConfig = new Yaml(repr);

    config.put("service", new HashMap<>());   
    ((Map) config.get("service")).put("repos", repos);

  }

  public RepoConfigBuilder fileRepo(String name, String uri) {
    FileRepoDef fileRepo = new FileRepoDef(name);
    fileRepo.setUri(uri);
    
    repos.put(fileRepo.getName(), fileRepo);
    return this;
  }
  
  public String build() {
    return repoConfig.dump(config);
  }

}

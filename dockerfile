FROM msusel/pique-core:0.9.5_2



# symlink to jar file for cleanliness
RUN ln -s /home/msusecl-pique-plc-customcode/target/msusel-pique-plc-customcode-$PIQUE_DOCKERFILE_VERSION-jar-with-dependencies.jar \
        /home/msusecl-pique-plc-customcode/entrypoint.jar


##### secret sauce
#ENTRYPOINT ["java", "-jar", "/home/msusecl-pique-plc-customcode/entrypoint.jar", "--run", "evaluate", "--file", "/input/custom-code-target.json"]
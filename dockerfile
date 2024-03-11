FROM msusel/pique-core:0.9.5_2

ARG PIQUE_PLC_CUSTOMCODE_VERSION=1.0

# move to home for a fresh start
WORKDIR "/home"

# install pipque plc customcode
RUN git clone https://github.com/MSUSEL/msusecl-pique-plc-customcode
WORKDIR "/home/msusecl-pique-plc-customcode"

# build pique plc customcode
RUN mvn package -Dmaven.test.skip

# create input directory
RUN mkdir "/input"

# input for project files
VOLUME ["/input"]

# output for model
VOLUME ["/output"]

# symlink to jar file for cleanliness
RUN ln -s /home/msusecl-pique-plc-customcode/target/msusel-pique-plc-customcode-PIQUE_PLC_CUSTOMCODE_VERSION-jar-with-dependencies.jar \
        /home/msusecl-pique-plc-customcode/entrypoint.jar


##### secret sauce
#ENTRYPOINT ["java", "-jar", "/home/msusecl-pique-plc-customcode/entrypoint.jar", "--run", "evaluate", "--file", "/input/custom-code-target.json"]
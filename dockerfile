#
# MIT License
#
# Copyright (c) 2024 Montana State University Software Engineering Labs
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#

FROM msusel/pique-core:0.9.5_2

ARG PIQUE_PLC_CUSTOMCODE_VERSION=1.0.0

# move to home for a fresh start
WORKDIR "/home"

# install pipque plc customcode
RUN git clone https://github.com/MSUSEL/msusecl-pique-plc-customcode
WORKDIR "/home/msusecl-pique-plc-customcode"

# build pique plc customcode
RUN mvn clean package -Dmaven.test.skip -Dlicense.skip

# create input directory
RUN mkdir "/input"

# input for project files
VOLUME ["/input"]

# output for model
VOLUME ["/output"]

# symlink to jar file for cleanliness
RUN ln -s /home/msusecl-pique-plc-customcode/target/msusel-pique-plc-customcode-$PIQUE_PLC_CUSTOMCODE_VERSION-jar-with-dependencies.jar \
         /home/msusecl-pique-plc-customcode/entrypoint.jar

##### secret sauce
#ENTRYPOINT ["java", "-jar", "/home/msusecl-pique-plc-customcode/entrypoint.jar", "--run", "evaluate"]
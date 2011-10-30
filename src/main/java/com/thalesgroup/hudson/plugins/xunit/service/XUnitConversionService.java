/*******************************************************************************
 * Copyright (c) 2010 Thales Corporate Services SAS                             *
 * Author : Gregory Boissinot                                                   *
 *                                                                              *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *******************************************************************************/

package com.thalesgroup.hudson.plugins.xunit.service;

import com.google.inject.Inject;
import com.thalesgroup.dtkit.metrics.model.InputMetric;
import com.thalesgroup.dtkit.util.converter.ConversionException;
import com.thalesgroup.hudson.plugins.xunit.exception.XUnitException;
import com.thalesgroup.hudson.plugins.xunit.types.CustomInputMetric;
import hudson.FilePath;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;


public class XUnitConversionService extends XUnitService implements Serializable {

    private XUnitLog xUnitLog;

    @Inject
    @SuppressWarnings("unused")
    void load(XUnitLog xUnitLog) {
        this.xUnitLog = xUnitLog;
    }

//    /**
//     * Prepares the conversion by adding specific behavior for the CustomType
//     *
//     * @param xUnitToolInfo the xUnit info wrapper object
//     * @param workspace     the current workspace
//     * @throws com.thalesgroup.hudson.plugins.xunit.exception.XUnitException
//     *          an XUnitException is thrown if there is a preparation error.
//     */
//    private void prepareConversion(XUnitToolInfo xUnitToolInfo, File workspace) throws XUnitException {
//        TestType testType = xUnitToolInfo.getTestType();
//        InputMetric inputMetric  = xUnitToolInfo.getInputMetric();
//        if (testType.getClass() == CustomInputMetric.class) {
//            String xsl = ((CustomType) testType).getCustomXSL();
//            File xslFile = new File(workspace, xsl);
//            if (!xslFile.exists()) {
//                throw new XUnitException("The input xsl '" + xsl + "' relative to the workspace '" + workspace + "'doesn't exist.");
//            }
//            xUnitToolInfo.setCusXSLFile(xslFile);
//        }
//    }


    /**
     * Converts the inputFile into a JUnit output file
     *
     * @param xUnitToolInfo        the xUnit info wrapper object
     * @param inputFile            the input file to be converted
     * @param workspace            the workspace
     * @param junitOutputDirectory the output parent directory that contains the JUnit output file
     * @return the converted file
     * @throws com.thalesgroup.hudson.plugins.xunit.exception.XUnitException
     *          an XUnitException is thrown if there is a conversion error.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public File convert(XUnitToolInfo xUnitToolInfo, File inputFile, File workspace, File junitOutputDirectory) throws XUnitException {

        //prepareConversion(xUnitToolInfo, workspace);

        InputMetric inputMetric = xUnitToolInfo.getInputMetric();

        final String JUNIT_FILE_POSTFIX = ".xml";
        final String JUNIT_FILE_PREFIX = "TEST-";
        File parent = new File(junitOutputDirectory, inputMetric.getToolName());
        parent.mkdirs();
        if (!parent.exists()) {
            throw new XUnitException("Can't create " + parent);
        }
        File junitTargetFile = new File(parent, JUNIT_FILE_PREFIX + inputFile.hashCode() + JUNIT_FILE_POSTFIX);
        infoSystemLogger("Converting '" + inputFile + "' .");
        try {


            if (inputMetric instanceof CustomInputMetric) {
                CustomInputMetric customInputMetric = (CustomInputMetric) inputMetric;
                FilePath xslFilePath = xUnitToolInfo.getCusXSLFile();
                if (!xslFilePath.exists()) {
                    throw new XUnitException("The input xsl '" + xslFilePath.getName() + "' relative to the workspace '" + workspace + "'doesn't exist.");
                }
                customInputMetric.setCustomXSLFile(new File(xUnitToolInfo.getCusXSLFile().getRemote()));
            }

            inputMetric.convert(inputFile, junitTargetFile);

        } catch (ConversionException ce) {
            throw new XUnitException("Conversion error " + ce.getMessage(), ce);
        } catch (InterruptedException ie) {
            throw new XUnitException("Conversion error " + ie.getMessage(), ie);
        } catch (IOException ie) {
            throw new XUnitException("Conversion error " + ie.getMessage(), ie);
        }

        return junitTargetFile;
    }


}

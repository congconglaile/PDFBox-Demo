package com.example.demo;

import com.lowagie.text.pdf.FdfReader;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import org.springframework.web.bind.annotation.*;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

@RestController
public class SampleController {

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public String upload(HttpServletRequest request) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        String resourcesPath = this.getClass().getResource("/").getPath();
        try{
            Part part = request.getPart("file");
            inputStream = part.getInputStream();
            File file = new File(resourcesPath + "PDF/step1/upload.pdf");
            file.createNewFile();
            part.write(resourcesPath + "PDF/step1/upload.pdf");
        }catch (Exception e){
            return "failed to upload!";
        }finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
        return "Upload successfully ^_^";
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        String fileName = "pdfWithForm.pdf";

        String resourcesPath = this.getClass().getResource("/").getPath();
        FileInputStream fileInputStream = null;
        String pdfFileWithForm = resourcesPath + "PDF/step1/" + fileName;

        /**
         *  Function: PDDocument.load(File file)
         *  Description: Parses a PDF. Load PDF to memory. Unrestricted main memory will be used for buffering PDF streams.
         *               pdfDocument is the in-memory representation of the PDF document.
         *              The #close() method must be called once the document is no longer needed.
         *  Parameters:File file
         *  Returns :loaded document.
         *  Throws: IOException - in case of a file reading or parsing error. InvalidPasswordException - If the file required a non-empty password.
         */
        try (PDDocument pdfDocument = PDDocument.load(new File(pdfFileWithForm))) {
            /**
             *  Function: getDocumentCatalog()
             *  Description: This will get the document CATALOG. This is guaranteed to not return null.
             *  Returns: The documents /Root dictionary
             */
            PDDocumentCatalog catalog = pdfDocument.getDocumentCatalog();

            /**
             *  Function: getAcroForm()
             *  Description: Get Get the documents AcroForm. This will return null if no AcroForm is part of the document.
             *  Returns: The document's AcroForm.
             */
            PDAcroForm acroForm = catalog.getAcroForm();

            // as there might not be an AcroForm entry a null check is necessary
            if (acroForm != null) {
                /**
                 *  Function: getField(String fullyQualifiedName)
                 *  Description: This will get a field by name, possibly using the cache if setCache is true.
                 *  Parameters: fullyQualifiedName - The name of the field to get.
                 *  Returns: The field with that name of null if one was not found.
                 */
                PDTextField field = (PDTextField) acroForm.getField("name");

                /**
                 *  Function: setValue(String value)
                 *  Description: Sets the plain text value of this field.
                 *  Parameters: value - Plain text
                 *  Throws: IOException - if the value could not be set
                 */
                field.setValue("Lynn");

                // set the other field
                field = (PDTextField) acroForm.getField("gender");
                field.setValue("female");
            }

            /**
             *  Function: save(File file)
             *  Description: Save the document to a file and close the filled out form.
             *  Parameters: file - The file to save as.
             *  Throws: IOException - if the output could not be written
             */
            pdfDocument.save(resourcesPath + "PDF/step2/" + fileName);

            // download the pdf with Patient Name
            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=pdfWithPatientName.pdf");

            OutputStream outputStream = response.getOutputStream();
            fileInputStream = new FileInputStream(new File(resourcesPath + "PDF/step2/" + fileName));
            IOUtils.copy(fileInputStream, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public String save(final HttpServletRequest request) {
        String contentType = request.getContentType();
        FdfReader reader = null;
        String name = "";
        String gender = "";
        String medicationName = "";
        String fileName = "submitInfo.pdf";
        String resourcesPath = this.getClass().getResource("/").getPath();

        if ("application/vnd.fdf".equalsIgnoreCase(contentType)) {
            try {
                // Parse the FDF data using iText
                reader = new FdfReader(request.getInputStream());
                HashMap map = reader.getFields();
                Set keys = map.keySet();
                for (Object key : keys) {
                    switch (key.toString()) {
                        case "name":
                            name = reader.getFieldValue(key.toString());
                            break;
                        case "gender":
                            gender = reader.getFieldValue(key.toString());
                            break;
                        case "medicationName":
                            medicationName = reader.getFieldValue(key.toString());
                            break;
                    }
                }
            } catch (Exception e) {
                return "Failed to submit!";
            } finally {
                reader.close();
            }

            // create new PDF
            try {
                PDDocument document = new PDDocument();
                PDPage page = new PDPage();
                document.addPage(page);

                PDFont font = PDType1Font.HELVETICA_BOLD;
                /**
                 *  Function: new PDPageContentStream(document, page)
                 *  Description: Create a new PDPage content stream.
                 *  Parameters: document - The document the page is part of.
                 *  Parameters: sourcePage - The page to write the contents to.
                 *  Throws: IOException  - If there is an error writing to the page contents.
                 */
                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                /**
                 *  Function: beginText()
                 *  Description: Begin some text operations.
                 *  Throws: IOException - If there is an error writing to the stream or if you attempt to nest beginText calls.
                 */
                contentStream.beginText();
                /**
                 *  Function: setFont(PDFont font, float fontSize)
                 *  Description: Set the font and font size to draw text with.
                 *  Parameters: font - The font to use.
                 *  Parameters: fontSize - The font size to draw the text.
                 *  Throws: IOException - If there is an error writing the font information.
                 */
                contentStream.setFont(font, 14);
                /**
                 *  Function: public void setLeading(double leading)
                 *  Description: Sets the text leading.
                 *  Parameters: leading - The leading in unscaled text units.
                 *  Throws: IOException - If there is an error writing to the stream.
                 */
                contentStream.setLeading(14 * 1.3);
                /**
                 *  Function: newLineAtOffset(float tx, float ty)
                 *  Description: The Td operator. Move to the start of the next line, offset from the start of the current line by (tx, ty).
                 *  Parameters: tx - The x translation.
                 *  Parameters: ty - The y translation.
                 *  Throws: IOException - If there is an error writing to the stream.
                 *  Throws: IllegalStateException - If the method was not allowed to be called at this time.
                 */
                contentStream.newLineAtOffset(50, 700);
                /**
                 *  Function: showText(String text)
                 *  Description: Shows the given text at the location specified by the current text matrix.
                 *  Parameters: text - The Unicode text to show.
                 *  Throws: IOException - If an io exception occurs.
                 */
                contentStream.showText("name: " + name);
                /**
                 *  Function: newLine()
                 *  Description: Move to the start of the next line of text. Requires the leading (see setLeading(double)) to have been set.
                 *  Throws: IOException - If there is an error writing to the stream.
                 */
                contentStream.newLine();
                contentStream.showText("gender: " + gender);
                contentStream.newLine();
                contentStream.showText("medicationName: " + medicationName);
                /**
                 *  Function: endText()
                 *  Description: End some text operations.
                 *  Throws: IOException - If there is an error writing to the stream or if you attempt to nest endText calls.
                 *  Throws: IllegalStateException - If the method was not allowed to be called at this time.
                 */
                contentStream.endText();
                /**
                 *  Function: close()
                 *  Description: Close the content stream. This must be called when you are done with this object.
                 *  Throws: IOException - If the underlying stream has a problem being written to.
                 */
                contentStream.close();

                document.save(resourcesPath + "PDF/step3/" + fileName);
                document.close();

            } catch (IOException e) {
                System.out.println(e.toString());
                return "Failed to submit!";
            }
        }
        return "Submit successfully ^_^";
    }

}

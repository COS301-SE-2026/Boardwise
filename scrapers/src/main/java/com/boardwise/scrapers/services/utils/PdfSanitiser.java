package com.boardwise.scrapers.services.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;

public class PdfSanitiser {
    
    private static final COSName AA = COSName.getPDFName("AA");
    private static final COSName OPEN_ACTION= COSName.getPDFName("OpenAction");
    private static final COSName AF = COSName.getPDFName("AF");
    private static final COSName NAMES = COSName.getPDFName("Names");
    private static final COSName JAVASCRIPT = COSName.getPDFName("Javascript");
    private static final COSName EMBEDDED_FILES = COSName.getPDFName("EmbeddedFiles");
    private static final COSName ACRO_FORM = COSName.getPDFName("AcroForm");
    private static final COSName XFA = COSName.getPDFName("XFA");
    private static final COSName OUTLINES = COSName.getPDFName("Outlines");
    private static final COSName A = COSName.getPDFName("A");
    private static final COSName S = COSName.getPDFName("S");
    private static final COSName NEXT = COSName.getPDFName("Next");
    private static final COSName GOTO = COSName.getPDFName("GoTo");
    private static final COSName URI = COSName.getPDFName("URI");

    private static final Set<String> BLOCKED_ANNOTATIONS = Set.of("FileAttachment", "Movie", "Sound", "Screen", "RichMedia", "3D");

    private PdfSanitiser(){}

    private static byte[] sanitise(byte[] pdfBytes) throws IOException{
        return sanitise(pdfBytes, false);
    }

    public static byte[] sanitise(byte[] pdfBytes, boolean dropFirstPage) throws IOException{

        try(PDDocument doc = Loader.loadPDF(pdfBytes)){
            doc.setAllSecurityToBeRemoved(true);

            COSDictionary catalog = doc.getDocumentCatalog().getCOSObject();
            catalog.removeItem(AA);
            catalog.removeItem(OPEN_ACTION);
            catalog.removeItem(AF);
            catalog.removeItem(OUTLINES);

            COSDictionary names = asDict(catalog.getDictionaryObject(NAMES));

            if(names != null){
                names.removeItem(JAVASCRIPT);
                names.removeItem(EMBEDDED_FILES);
            }

            for(PDPage page : doc.getPages()){
                cleanPage(page);
            }

            if(dropFirstPage && doc.getNumberOfPages() > 1){
                doc.removePage(0);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }
        private static void cleanPage(PDPage page) throws IOException{
            page.getCOSObject().removeItem(AA);

            List<PDAnnotation> annotations = page.getAnnotations();
            List<PDAnnotation> kept = new ArrayList<>();

            for(PDAnnotation annotation : annotations){
                if(BLOCKED_ANNOTATIONS.contains(annotation.getSubtype())){
                    continue;
                }

                COSDictionary dict = annotation.getCOSObject();
                dict.removeItem(AA);
                stripUnsafeAction(dict);
                kept.add(annotation);
            }

            if (kept.size() != annotations.size()) {
                page.setAnnotations(kept);
            }
    }

    private static void stripUnsafeAction(COSDictionary holder){
        COSDictionary action = asDict(holder.getDictionaryObject(A));

        if(action == null){
            return;
        }

        COSName type = action.getCOSName(S);

        if(GOTO.equals(type)|| URI.equals(type)){
            action.removeItem(NEXT);
        }
        else{
            holder.removeItem(A);
        }
    }

    private static COSDictionary asDict(COSBase base){
        return base instanceof COSDictionary dictionary ? dictionary: null; 
    }
}

package com.pdf.pdfReaderUsingHuggingFace;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info=@Info(title="Pdf Reader ",
                     version="v1",
description="This is PDF Reader"),
servers=@Server(url="/pdf"))
public class PdfReaderUsingHuggingFaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PdfReaderUsingHuggingFaceApplication.class, args);
		System.out.println("Backend just started....");
	}

}

package it.aruba.delivery.request.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import it.aruba.delivery.request.entity.DeliveryRequest;
import it.aruba.delivery.request.service.DeliveryService;

@WebMvcTest(DeliveryController.class)
class DeliveryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private DeliveryService service;

	@Test
	void shouldCreateDeliveryRequest() throws Exception {

	    when(service.create(any()))
	            .thenReturn(new DeliveryRequest());

	    mockMvc.perform(post("/delivery-requests")
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("""
	            {
	              "recipientIds": ["1"],
	              "requestType": "EMAIL",
	              "documentName": "test.pdf",
	              "documentType": "PDF"
	            }
	            """))
	            .andExpect(status().isCreated());
	}
}
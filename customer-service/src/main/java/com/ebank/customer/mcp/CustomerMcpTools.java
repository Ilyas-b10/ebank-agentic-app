package com.ebank.customer.mcp;

import com.ebank.customer.entities.Customer;
import com.ebank.customer.services.CustomerService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Outils MCP exposes par customer-service aux agents IA.
 */
@Component
public class CustomerMcpTools {

    private final CustomerService customerService;

    public CustomerMcpTools(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Tool(description = "Retourne la liste de tous les clients de la banque (id, nom, email)")
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @Tool(description = "Retourne un client de la banque a partir de son identifiant numerique")
    public Customer getCustomerById(@ToolParam(description = "Identifiant numerique du client") Long id) {
        return customerService.getCustomerById(id);
    }

    @Tool(description = "Recherche les clients dont le nom contient le texte fourni (insensible a la casse)")
    public List<Customer> searchCustomersByName(
            @ToolParam(description = "Nom ou partie du nom du client a rechercher") String name) {
        return customerService.searchByName(name);
    }

    @Tool(description = "Cree un nouveau client dans la banque et retourne le client cree")
    public Customer createCustomer(
            @ToolParam(description = "Nom du client") String name,
            @ToolParam(description = "Adresse email du client") String email) {
        return customerService.createCustomer(new Customer(null, name, email));
    }
}

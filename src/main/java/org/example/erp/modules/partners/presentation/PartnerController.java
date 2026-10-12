package org.example.erp.modules.partners.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.erp.modules.partners.application.actions.CreatePartner;
import org.example.erp.modules.partners.application.actions.FindPartner;
import org.example.erp.modules.partners.application.actions.ListPartners;
import org.example.erp.modules.partners.application.actions.UpdatePartner;
import org.example.erp.modules.partners.application.commands.SavePartnerCommand;
import org.example.erp.modules.partners.application.dto.PartnerDTO;
import org.example.erp.modules.partners.application.enums.PartnerKind;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/partners")
class PartnerController {
    private final ListPartners listPartners;
    private final FindPartner findPartner;
    private final CreatePartner createPartner;
    private final UpdatePartner updatePartner;

    PartnerController(ListPartners listPartners, FindPartner findPartner, CreatePartner createPartner, UpdatePartner updatePartner) {
        this.listPartners = listPartners;
        this.findPartner = findPartner;
        this.createPartner = createPartner;
        this.updatePartner = updatePartner;
    }

    record PartnerRequest(@NotBlank String name, @NotNull PartnerKind type, String contactPerson, String phone,
                          String email, String address, String taxId, Boolean active) {
        SavePartnerCommand toCommand() {
            return new SavePartnerCommand(name, type, contactPerson, phone, email, address, taxId, active == null || active);
        }
    }

    @GetMapping
    List<PartnerDTO> list() {
        return listPartners.handle();
    }

    @GetMapping("/{id}")
    PartnerDTO get(@PathVariable long id) {
        return findPartner.handle(id);
    }

    @PostMapping
    PartnerDTO create(@RequestBody @Valid PartnerRequest r) {
        return createPartner.handle(r.toCommand());
    }

    @PutMapping("/{id}")
    PartnerDTO update(@PathVariable long id, @RequestBody @Valid PartnerRequest r) {
        return updatePartner.handle(id, r.toCommand());
    }
}

package luxdine.unittest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import luxdine.example.luxdine.domain.catalog.dto.request.MenuItemRequest;
import luxdine.example.luxdine.domain.catalog.dto.request.MenuItemUpdateRequest;
import luxdine.example.luxdine.domain.catalog.dto.response.CategoryResponse;
import luxdine.example.luxdine.domain.catalog.dto.response.MenuItemDetailResponse;
import luxdine.example.luxdine.domain.catalog.enums.ItemVisibility;
import luxdine.example.luxdine.service.admin.AdminMenuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AdminMenuAPI.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminMenuAPITest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminMenuService adminMenuService;

    private MenuItemDetailResponse stubDetail(Long id, String name, Double price, Long categoryId) {
        CategoryResponse category = categoryId == null ? null :
                CategoryResponse.builder().id(categoryId).name("Cat-" + categoryId).slug("cat-" + categoryId).build();
        return MenuItemDetailResponse.builder()
                .id(id)
                .name(name)
                .price(price)
                .category(category)
                .visibility(ItemVisibility.PUBLIC.name())
                .isAvailable(true)
                .build();
    }

    @DisplayName("POST /api/admin/menu - create menu item from CSV")
    @ParameterizedTest
    @CsvFileSource(resources = "/admin_menu_create.csv", numLinesToSkip = 1, nullValues = {"null"})
    void createMenuItem_fromCsv(String name,
                                String description,
                                double price,
                                String slug,
                                String imageUrl,
                                String visibility,
                                String available,
                                String categoryIdStr) throws Exception {
        Long categoryId = categoryIdStr == null ? null : Long.parseLong(categoryIdStr);
        Boolean availableBool = available == null ? null : Boolean.parseBoolean(available);

        Mockito.when(adminMenuService.createMenuItem(any(MenuItemRequest.class)))
                .thenAnswer(inv -> {
                    MenuItemRequest req = inv.getArgument(0);
                    return stubDetail(101L, req.getName(), req.getPrice(), req.getCategoryId());
                });

        MenuItemRequest req = MenuItemRequest.builder()
                .name(name)
                .description(description)
                .price(price)
                .slug(slug)
                .imageUrl(imageUrl)
                .visibility(visibility)
                .available(availableBool)
                .categoryId(categoryId)
                .build();

        mockMvc.perform(
                        post("/api/admin/menu")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(101))
                .andExpect(jsonPath("$.data.name").value(name))
                .andExpect(jsonPath("$.data.price").value(price));
    }

    @DisplayName("PUT /api/admin/menu/{id} - update menu item from CSV")
    @ParameterizedTest
    @CsvFileSource(resources = "/admin_menu_update.csv", numLinesToSkip = 1, nullValues = {"null"})
    void updateMenuItem_fromCsv(long id,
                                String name,
                                String description,
                                double price,
                                String categoryIdStr,
                                String imageUrl,
                                String visibility,
                                String isAvailable) throws Exception {
        Long categoryId = categoryIdStr == null ? null : Long.parseLong(categoryIdStr);
        Boolean availableBool = isAvailable == null ? null : Boolean.parseBoolean(isAvailable);
        ItemVisibility vis = visibility == null ? null : ItemVisibility.valueOf(visibility);

        Mockito.when(adminMenuService.updateMenuItem(eq(id), any(MenuItemUpdateRequest.class)))
                .thenAnswer(inv -> {
                    MenuItemUpdateRequest req = inv.getArgument(1);
                    return stubDetail(id, req.getName(), req.getPrice(), req.getCategoryId());
                });

        MenuItemUpdateRequest req = MenuItemUpdateRequest.builder()
                .name(name)
                .description(description)
                .price(price)
                .categoryId(categoryId)
                .imageUrl(imageUrl)
                .visibility(vis)
                .isAvailable(availableBool)
                .build();

        mockMvc.perform(
                        put("/api/admin/menu/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value((int) id))
                .andExpect(jsonPath("$.data.name").value(name))
                .andExpect(jsonPath("$.data.price").value(price));
    }

    @DisplayName("DELETE /api/admin/menu/{id} - delete menu item")
    @org.junit.jupiter.api.Test
    void deleteMenuItem_shouldReturnOk() throws Exception {
        Mockito.doNothing().when(adminMenuService).deleteMenuItem(999L);

        mockMvc.perform(
                        delete("/api/admin/menu/{id}", 999L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Menu item deleted successfully"));
    }
}



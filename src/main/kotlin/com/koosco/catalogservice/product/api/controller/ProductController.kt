package com.koosco.catalogservice.product.api.controller

import com.koosco.catalogservice.product.api.dto.ProductCreateRequest
import com.koosco.catalogservice.product.api.dto.ProductDetailResponse
import com.koosco.catalogservice.product.api.dto.ProductListResponse
import com.koosco.catalogservice.product.api.dto.ProductUpdateRequest
import com.koosco.catalogservice.product.application.dto.*
import com.koosco.catalogservice.product.application.usecase.*
import com.koosco.common.core.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Product", description = "Product management APIs")
@RestController
@RequestMapping("/api/catalog/products")
class ProductController(
    private val getProductListUseCase: GetProductListUseCase,
    private val getProductDetailUseCase: GetProductDetailUseCase,
    private val createProductUseCase: CreateProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
) {
    @Operation(summary = "상품 리스트를 조회합니다.", description = "필터링 조건에 따라 상품을 페이징처리하여 조회합니다.")
    @GetMapping
    fun getProducts(
        @Parameter(description = "카테고리 ID") @RequestParam(required = false) categoryId: Long?,
        @Parameter(description = "이름 또는 상품 설명") @RequestParam(required = false) keyword: String?,
        @Parameter(description = "페이징 파라미터 (page, size, sort)") @PageableDefault(size = 20) pageable: Pageable,
    ): ApiResponse<Page<ProductListResponse>> {
        val command = GetProductListCommand(
            categoryId = categoryId,
            keyword = keyword,
            pageable = pageable,
        )

        return ApiResponse.success(getProductListUseCase.execute(command).map { ProductListResponse.from(it) })
    }

    @Operation(summary = "상품 상세를 조회합니다.", description = "옵션을 포함하여 상품을 조회합니다.")
    @GetMapping("/{productId}")
    fun getProduct(
        @Parameter(description = "Product ID") @PathVariable productId: Long,
    ): ApiResponse<ProductDetailResponse> {
        val command = GetProductDetailCommand(productId = productId)
        val productInfo = getProductDetailUseCase.execute(command)

        return ApiResponse.success(ProductDetailResponse.from(productInfo))
    }

    @Operation(
        summary = "새로운 상품을 추가합니다.",
        description = "상품 옵션과 함께 상품을 생성합니다. 판매자만 등록이 가능합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createProduct(@Valid @RequestBody request: ProductCreateRequest): ApiResponse<ProductDetailResponse> {
        val productInfo = createProductUseCase.execute(request.toCommand())

        return ApiResponse.success(ProductDetailResponse.from(productInfo))
    }

    @Operation(
        summary = "상품 정보를 업데이트합니다.",
        description = "상품 정보를 업데이트합니다. 판매자만 수정이 가능합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @PutMapping("/{productId}")
    fun updateProduct(
        @Parameter(description = "Product ID") @PathVariable productId: Long,
        @Valid @RequestBody request: ProductUpdateRequest,
    ): ApiResponse<Any> {
        updateProductUseCase.execute(request.toCommand(productId))

        return ApiResponse.success()
    }

    @Operation(
        summary = "상품을 삭제합니다.",
        description = "상품을 삭제합니다. 판매자만 삭제가 가능합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProduct(@Parameter(description = "Product ID") @PathVariable productId: Long): ApiResponse<Any> {
        deleteProductUseCase.execute(DeleteProductCommand(productId = productId))

        return ApiResponse.success()
    }
}

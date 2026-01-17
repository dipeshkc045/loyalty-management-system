package com.lms.member.controller;

import com.lms.member.dto.DashboardStats;
import com.lms.member.dto.MemberDTO;
import com.lms.member.model.Member;
import com.lms.member.model.MemberTier;
import com.lms.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "Member Management", description = "APIs for managing customer members, tiers, and loyalty points")
public class MemberController {
        private final MemberService memberService;

        @PostMapping
        @Operation(summary = "Create a new member", description = "Register a new customer member in the loyalty program")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Member created successfully", content = @Content(schema = @Schema(implementation = Member.class))),
                        @ApiResponse(responseCode = "409", description = "Member with this email already exists", content = @Content),
                        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content)
        })
        public ResponseEntity<Member> createMember(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Member details to create", required = true, content = @Content(schema = @Schema(implementation = Member.class))) @RequestBody Member member) {
                return ResponseEntity.ok(memberService.createMember(member));
        }

        @GetMapping
        @Operation(summary = "Get all members with pagination", description = "Retrieve paginated list of members with optional search and tier filtering")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Members retrieved successfully", content = @Content(schema = @Schema(implementation = Page.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters", content = @Content)
        })
        public ResponseEntity<Page<MemberDTO>> getAllMembers(
                        @Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,
                        @Parameter(description = "Search term for name, email, or phone") @RequestParam(required = false) String search,
                        @Parameter(description = "Filter by tier") @RequestParam(required = false) MemberTier tier) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
                Page<MemberDTO> members = memberService.getAllMembers(pageable, search, tier);
                return ResponseEntity.ok()
                                .body(members);
        }

        @GetMapping("/stats")
        @Operation(summary = "Get dashboard statistics", description = "Retrieve aggregated statistics for dashboard display")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully", content = @Content(schema = @Schema(implementation = DashboardStats.class)))
        })
        public ResponseEntity<DashboardStats> getDashboardStats() {
                DashboardStats stats = memberService.getDashboardStats();
                return ResponseEntity.ok()
                                .body(stats);
        }

        @GetMapping("/lite")
        @Operation(summary = "Get lightweight member list", description = "Retrieve a limited list of members for dropdowns and autocomplete")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Member list retrieved successfully", content = @Content(schema = @Schema(implementation = List.class)))
        })
        public ResponseEntity<List<MemberDTO>> getMembersLite(
                        @Parameter(description = "Maximum number of members to return", example = "100") @RequestParam(defaultValue = "100") int limit) {
                List<MemberDTO> members = memberService.getMembersLite(limit);
                return ResponseEntity.ok()
                                .body(members);
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get member by ID", description = "Retrieve member details including tier and points")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Member found", content = @Content(schema = @Schema(implementation = Member.class))),
                        @ApiResponse(responseCode = "404", description = "Member not found", content = @Content)
        })
        public ResponseEntity<Member> getMember(
                        @Parameter(description = "Member ID", required = true, example = "1") @PathVariable Long id) {
                return memberService.getMember(id)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update member", description = "Update member profile information")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Member updated successfully", content = @Content(schema = @Schema(implementation = Member.class))),
                        @ApiResponse(responseCode = "404", description = "Member not found", content = @Content)
        })
        public ResponseEntity<Member> updateMember(
                        @Parameter(description = "Member ID", required = true, example = "1") @PathVariable Long id,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated member details", required = true, content = @Content(schema = @Schema(implementation = Member.class))) @RequestBody Member member) {
                return ResponseEntity.ok(memberService.updateMember(id, member));
        }

        @GetMapping("/{id}/points")
        @Operation(summary = "Get member points", description = "Retrieve current point balance for a member")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Points retrieved successfully", content = @Content(schema = @Schema(implementation = Integer.class))),
                        @ApiResponse(responseCode = "404", description = "Member not found", content = @Content)
        })
        public ResponseEntity<Integer> getPoints(
                        @Parameter(description = "Member ID", required = true, example = "1") @PathVariable Long id) {
                return ResponseEntity.ok(memberService.getPointBalance(id));
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete member", description = "Remove a member from the system")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Member deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Member not found")
        })
        public ResponseEntity<Void> deleteMember(
                        @Parameter(description = "Member ID", required = true, example = "1") @PathVariable Long id) {
                memberService.deleteMember(id);
                return ResponseEntity.noContent().build();
        }

        @PostMapping("/{id}/reset")
        @Operation(summary = "Reset member points", description = "Reset member points to Onboarding amount (500) and clear history")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Member reset successfully"),
                        @ApiResponse(responseCode = "404", description = "Member not found")
        })
        public ResponseEntity<Void> resetMember(
                        @Parameter(description = "Member ID", required = true, example = "1") @PathVariable Long id) {
                memberService.resetMemberPoints(id);
                return ResponseEntity.ok().build();
        }
}

package com.wargameclub.clubapi.service;

import java.time.OffsetDateTime;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wargameclub.clubapi.entity.Booking;
import com.wargameclub.clubapi.entity.ClubTable;
import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.enums.BookingStatus;
import com.wargameclub.clubapi.exception.ConflictException;
import com.wargameclub.clubapi.repository.BookingRepository;
import com.wargameclub.clubapi.repository.ClubTableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableAllocationServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ClubTableRepository tableRepository;

    private TableAllocationService service;

    @BeforeEach
    void setUp() {
        service = new TableAllocationService(bookingRepository, tableRepository, new ObjectMapper());
    }

    @Test
    void allocateForRangeRespectsPreferredTable() {
        ClubTable table1 = table("T1", 1L);
        ClubTable table2 = table("T2", 2L);

        when(tableRepository.findActiveForUpdate()).thenReturn(List.of(table1, table2));
        when(bookingRepository.findOverlappingWithDetails(eq(BookingStatus.CREATED), any(), any()))
                .thenReturn(List.of());

        OffsetDateTime start = OffsetDateTime.now().plusDays(1);
        OffsetDateTime end = start.plusHours(2);
        TableAllocationService.AllocationSnapshot snapshot = service.allocateForRange(start, end, 2, 1L);

        assertThat(snapshot.allocations()).hasSize(1);
        assertThat(snapshot.allocations().get(0).tableId()).isEqualTo(1L);
        assertThat(snapshot.allocations().get(0).units()).isEqualTo(2);
    }

    @Test
    void allocateForRangeRejectsWhenCapacityMissing() {
        ClubTable table1 = table("T1", 1L);
        User owner = new User("Owner");
        Booking overlapping = new Booking(table1, owner, OffsetDateTime.now(), OffsetDateTime.now().plusHours(2));
        overlapping.setTableAssignments("[{\"tableId\":1,\"units\":2}]");

        when(tableRepository.findActiveForUpdate()).thenReturn(List.of(table1));
        when(bookingRepository.findOverlappingWithDetails(eq(BookingStatus.CREATED), any(), any()))
                .thenReturn(List.of(overlapping));

        OffsetDateTime start = OffsetDateTime.now().plusDays(1);
        OffsetDateTime end = start.plusHours(2);
        assertThatThrownBy(() -> service.allocateForRange(start, end, 2, null))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void parseAllocationsFallsBackToLegacyFieldsOnBrokenJson() {
        ClubTable table = table("T1", 5L);
        Booking booking = new Booking(table, new User("User"), OffsetDateTime.now(), OffsetDateTime.now().plusHours(2));
        booking.setTableUnits(2);
        booking.setTableAssignments("{broken-json");

        List<TableAllocationService.TableAllocation> parsed = service.parseAllocations(booking);

        assertThat(parsed).hasSize(1);
        assertThat(parsed.get(0).tableId()).isEqualTo(5L);
        assertThat(parsed.get(0).units()).isEqualTo(2);
    }

    private ClubTable table(String name, Long id) {
        ClubTable table = new ClubTable(name, true, null);
        ReflectionTestUtils.setField(table, "id", id);
        return table;
    }
}

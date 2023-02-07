package nextstep.subway.unit;

import static nextstep.subway.common.LineFixtures.*;
import static nextstep.subway.common.StationFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.util.ReflectionUtils;

import nextstep.subway.domain.Line;
import nextstep.subway.domain.Section;
import nextstep.subway.domain.Station;
import nextstep.subway.domain.exception.InvalidLineUpdateException;
import nextstep.subway.domain.exception.LineErrorCode;
import nextstep.subway.domain.exception.SectionErrorCode;
import nextstep.subway.domain.exception.SectionRemoveException;

class LineTest {

	@DisplayName("노선 구간추가에 성공한다")
	@Test
	void 노선_구간추가에_성공한다() throws Exception {
		// given
		Line line = LINE_4();
		int distance = 10;

		// when
		line.addSection(withId(동대문역사문화공원, 동대문역사문화공원_ID), withId(충무로, 충무로_ID), distance);
		List<Section> sections = line.getSections();

		//then
		assertThat(sections).hasSize(2);
	}

	@DisplayName("노선에 포함된 역들조회에 성공한다")
	@Test
	void 노선에_포함된_역조회에_성공한다() throws Exception {
		// given
		Line line = LINE_4();

		// when
		List<Station> stations = line.getStations();

		// then
		assertThat(stations).containsExactly(withId(동대문, 동대문_ID), withId(동대문역사문화공원, 동대문역사문화공원_ID));
	}

	@DisplayName("구간이 한개 이상일때 마지막 구간 제거에 성공한다")
	@Test
	void 구간이_한개_이상일때_마지막_구간_제거에_성공한다() throws Exception {
		// given
		Line line = LINE_4();
		line.addSection(withId(동대문역사문화공원, 동대문역사문화공원_ID), withId(충무로, 충무로_ID), 5);
		insertIdInSections(line.getSections());

		// when
		line.removeSection(충무로);

		// then
		assertAll(
			() -> assertThat(line.getDownStationId()).isEqualTo(동대문역사문화공원_ID),
			() -> assertThat(line.getSections()).hasSize(1)
		);
	}

	@DisplayName("구간제거시 상행종점역과 하행종점역만 있을경우 예외가 발생한다")
	@Test
	void 구간제거시_상행종점역과_하행종점역만_있을경우_예외가_발생한다() throws Exception {
		Line line = LINE_4();

		assertThatThrownBy(() -> line.removeSection(withId(동대문역사문화공원, 동대문역사문화공원_ID)))
			.isInstanceOf(SectionRemoveException.class)
			.hasMessage(SectionErrorCode.SINGLE_SECTION.getMessage());

	}

	@DisplayName("구간제거시 제거할구간이 하행종점역이 아닐경우 예외가 발생한다")
	@Test
	void 구간제거시_제거할구간이_하행종점역이_아닐경우_예외가_발생한다() throws Exception {
		Line line = LINE_4();
		line.addSection(withId(동대문역사문화공원, 동대문역사문화공원_ID), withId(충무로, 충무로_ID), 5);
		insertIdInSections(line.getSections());

		assertThatThrownBy(() -> line.removeSection(withId(동대문역사문화공원, 동대문역사문화공원_ID)))
			.isInstanceOf(SectionRemoveException.class)
			.hasMessage(SectionErrorCode.INVALID_REMOVE_STATION.getMessage());
	}

	@DisplayName("구간제거시 제거할 지하철역이 노선에 포함되지않을경우 예외가 발생한다")
	@Test
	void 구간제거시_제거할_지하철역이_노선에_포함되지않을경우_예외가_발생한다() throws Exception {
		Line line = LINE_4();
		line.addSection(withId(동대문역사문화공원, 동대문역사문화공원_ID), withId(충무로, 충무로_ID), 5);
		insertIdInSections(line.getSections());

		assertThatThrownBy(() -> line.removeSection(withId(서울역, 서울역_ID)))
			.isInstanceOf(SectionRemoveException.class)
			.hasMessage(SectionErrorCode.NOT_INCLUDE_STATION.getMessage());
	}

	@Test
	void 지하철노선_정보_수정에_성공한다() throws Exception {
		// given
		Line line = LINE_4();
		String name = "2호선";
		String color = "green";

		// when
		line.updateInfo(
			name,
			color
		);

		// then
		assertAll(
			() -> assertThat(line.getName()).isEqualTo(name),
			() -> assertThat(line.getColor()).isEqualTo(color)
		);
	}

	@DisplayName("지하철노선 정보 수정시 이름이 null혹은 empty일경우 예외가 발생한다")
	@ParameterizedTest
	@NullAndEmptySource
	void 지하철노선_정보_수정시_이름이_null혹은_empty일경우_예외가_발생한다(String name) {
		assertThatThrownBy(() -> LINE_4().updateInfo(name, "color"))
			.isInstanceOf(InvalidLineUpdateException.class)
			.hasMessage(LineErrorCode.INVALID_NAME_UPDATER_REQUEST.getMessage());
	}

	@DisplayName("지하철노선 정보 수정시 지하철색이 null혹은 empty일경우 예외가 발생한다")
	@ParameterizedTest
	@NullAndEmptySource
	void 지하철노선_정보_수정시_지하철색이_null혹은_empty일경우_예외가_발생한다(String color) {
		assertThatThrownBy(() -> LINE_4().updateInfo("2호선", color))
			.isInstanceOf(InvalidLineUpdateException.class)
			.hasMessage(LineErrorCode.INVALID_COLOR_UPDATE_REQUEST.getMessage());
	}

	private void insertIdInSections(List<Section> sections) {
		for (int i = 1; i <= sections.size(); i++) {
			Section section = sections.get(i - 1);
			Field idField = ReflectionUtils.findField(section.getClass(), "id");
			ReflectionUtils.makeAccessible(idField);
			ReflectionUtils.setField(idField, section, (long)i);
		}
	}
}

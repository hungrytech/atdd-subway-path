package nextstep.subway.domain.exception;

public enum SectionErrorCode implements ErrorCode {
	SINGLE_SECTION("지하철 노선에 상행 종점역과 하행 종점역만 존재합니다."),
	INVALID_REMOVE_STATION("구간 제거 시 하행종점역만 가능합니다."),
	NOT_INCLUDE_STATION("노선에 포함되지 않은 지하철역 입니다.");

	private final String message;

	SectionErrorCode(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	@Override
	public String getCode() {
		return this.name();
	}
}

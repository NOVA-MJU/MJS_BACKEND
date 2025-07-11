package nova.mjs.domain.weather;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "weather")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private double temperature;

    @Column(nullable = false)
    private double feelsLike;

    @Column(nullable = false)
    private int humidity;

    @Column(nullable = false)
    private String weatherMain; // 날씨 그룹 (예: Rain, Clear)

    @Column(nullable = false)
    private String weatherDescription; // 상세 설명 (예: light rain)

    @Column(nullable = false)
    private String weatherIcon; // 날씨 아이콘 코드 (예: 10n)

    @Column(nullable = false)
    private double minTemperature;

    @Column(nullable = false)
    private double maxTemperature;

    @Column(nullable = false)
    private double pm10;

    @Column(nullable = false)
    private double pm2_5;

    @Column(nullable = false)
    private String pm10Category;

    @Column(nullable = false)
    private String pm2_5Category;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
package club.hiraeth.ionosonde.ui.components

object IndexExplanations {
    val explanations = mapOf(
        "SFI" to "Solar Flux Index measures radio emissions from the Sun at 2800 MHz (10.7 cm wavelength). " +
                "Higher values (100+) generally mean better HF propagation, especially on higher bands (10-20m). " +
                "Values below 80 typically mean poor conditions on bands above 20m.",
        "SN" to "Sunspot Number counts visible sunspots on the solar disk. More sunspots correlate with " +
                "higher solar activity and better HF propagation. During solar maximum, values can exceed 150.",
        "A-Index" to "The A-index is a daily average of geomagnetic activity derived from K-index values. " +
                "Lower values (below 10) indicate quiet conditions favourable for HF propagation. " +
                "Higher values indicate disturbed conditions that can degrade HF signals.",
        "K-Index" to "The planetary K-index (Kp) measures geomagnetic disturbance on a 0-9 scale. " +
                "K0-2: Quiet, good HF propagation. K3-4: Unsettled, some degradation possible. " +
                "K5+: Storm conditions, significant HF disruption, aurora possible.",
        "X-Ray" to "X-ray flux classification of solar flares. A and B: background/quiet. " +
                "C-class: small flares, minor impact. M-class: moderate flares, possible brief HF blackouts. " +
                "X-class: major flares, HF blackouts on the sunlit side of Earth.",
        "Proton Flux" to "Energetic proton flux in particle flux units (pfu). Elevated levels (10+ pfu) " +
                "indicate a solar proton event that can cause polar cap absorption, severely degrading " +
                "HF propagation on polar and trans-polar paths.",
        "Electron Flux" to "High-energy electron flux at geostationary orbit. Elevated levels can indicate " +
                "enhanced radiation belt activity, which may affect satellite operations and correlate " +
                "with disturbed geomagnetic conditions.",
        "Solar Wind" to "Speed of the solar wind in km/s. Normal speed is 300-400 km/s. " +
                "Elevated speeds (500+ km/s) can trigger geomagnetic storms and degrade HF propagation, " +
                "particularly at high latitudes.",
        "Bz" to "The north-south component of the interplanetary magnetic field (IMF). " +
                "When Bz is southward (negative), it couples more effectively with Earth's magnetosphere, " +
                "increasing the chance of geomagnetic storms and aurora.",
        "Geo Mag" to "Current geomagnetic field status from quiet to major storm. " +
                "Quiet conditions favour stable HF propagation. Active to storm conditions " +
                "can cause signal fading, absorption, and path disruption.",
        "Signal Noise" to "General signal noise level. Higher noise levels reduce the signal-to-noise ratio, " +
                "making weak signals harder to copy. Can be elevated during geomagnetic storms " +
                "and in the presence of solar radio bursts.",
        "Aurora" to "Aurora activity indicator. When aurora is active, VHF signals can be reflected " +
                "off the auroral curtain (aurora propagation on 2m/6m), but HF signals on polar paths " +
                "are typically absorbed.",
        "Aurora Lat" to "Estimated latitude at which aurora may be visible. Lower latitudes indicate " +
                "stronger geomagnetic activity. At K5, aurora may be visible around 55°N; at K7+, " +
                "it can extend to 45°N or lower.",
        "Flare Prob" to "Probability of solar flares occurring in the next 24 hours. " +
                "Higher probabilities suggest increased chance of HF radio blackouts " +
                "and potential geomagnetic storms 1-3 days after a coronal mass ejection.",
        "MUF" to "Maximum Usable Frequency — the highest frequency that will reflect off the " +
                "ionosphere for a given path. Frequencies above the MUF will pass through to space. " +
                "Higher MUF values mean higher bands (10m, 12m) may be open.",
        "EME Deg" to "Earth-Moon-Earth (moonbounce) degradation. Lower values are better for EME. " +
                "Indicates the expected signal degradation for signals bounced off the lunar surface.",
    )
}

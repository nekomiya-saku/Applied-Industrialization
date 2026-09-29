"""Download and verify compile-only dependencies for the CI build."""

import hashlib
from pathlib import Path
from urllib.request import Request, urlopen


DEPENDENCIES = (
    (
        "libs/appliedenergistics2-19.2.17.jar",
        "https://cdn.modrinth.com/data/XxWD5pD3/versions/kfyIqgJ6/appliedenergistics2-19.2.17.jar",
        "55edfd948366aff620881e0625e48c333a2cb847e73249bc0b588efbc4b86709992a8ffbca97ea387e270df4186fe7f74ee2f27b739f1c952e932becfb9dea33",
    ),
    (
        "libs/Modern-Industrialization-2.5.6.jar",
        "https://cdn.modrinth.com/data/Gov5Dboq/versions/E1nD4PKl/Modern-Industrialization-2.5.6.jar",
        "ee98da11ae52892ce5e60ef87367bf681b3c8e2184200337a64cc71e16133ae1cabb2dd4016e6dd200b476d6d7378aed0f82fa65026150e63aec0ff09bac3b3a",
    ),
    (
        "libs/Jade-1.21.1-NeoForge-15.10.6.jar",
        "https://cdn.modrinth.com/data/nvQzSEkH/versions/eYz2YBGT/Jade-1.21.1-NeoForge-15.10.6.jar",
        "dad9755dce8d85d914fc4df2baa0211f13e5839a71c1925fdd01f69081a95e30a2934e6273f8b01f0169adebe7a9dae57a8d904de0c4cb36dc17369bb474f0f2",
    ),
)


def main() -> None:
    Path("libs").mkdir(parents=True, exist_ok=True)
    for filename, url, expected_sha512 in DEPENDENCIES:
        print(f"Downloading {filename} ...", flush=True)
        request = Request(url, headers={"User-Agent": "Applied-Industrialization-CI/1.2.7"})
        with urlopen(request, timeout=300) as response:
            data = response.read()
        actual_sha512 = hashlib.sha512(data).hexdigest()
        print(f"Downloaded {len(data)} bytes; sha512={actual_sha512}", flush=True)
        if actual_sha512 != expected_sha512:
            raise RuntimeError(f"SHA-512 mismatch for {filename}")
        Path(filename).write_bytes(data)


if __name__ == "__main__":
    main()

$ErrorActionPreference = 'Stop'

$workspace = 'D:\project\youda-project'
$tempRoot = Join-Path $workspace '.open-course-import'
$videoDir = Join-Path $tempRoot 'videos'
$coverDir = Join-Path $tempRoot 'covers'

New-Item -ItemType Directory -Force -Path $videoDir | Out-Null
New-Item -ItemType Directory -Force -Path $coverDir | Out-Null

$assets = @(
  @{
    Url = 'https://www.archive.org/download/MIT3_091SCF10/MIT3_091SCF10lec01_300k.mp4'
    Path = (Join-Path $videoDir 'mit-chemistry-intro.mp4')
    MinBytes = 50000000
  },
  @{
    Url = 'https://www.archive.org/download/MIT3_091SCF10/MIT3_091SCF10lec02_300k.mp4'
    Path = (Join-Path $videoDir 'mit-chemistry-periodic-table.mp4')
    MinBytes = 50000000
  },
  @{
    Url = 'https://archive.org/download/MIT15.084JS04/mit-ocw-15.084j-freund-10feb2004-220k.mp4'
    Path = (Join-Path $videoDir 'mit-nonlinear-10feb2004.mp4')
    MinBytes = 50000000
  },
  @{
    Url = 'https://archive.org/download/MIT15.084JS04/mit-ocw-15.084j-freund-03may2004-220k.mp4'
    Path = (Join-Path $videoDir 'mit-nonlinear-03may2004.mp4')
    MinBytes = 50000000
  }
)

foreach ($asset in $assets) {
  $needsDownload = $true
  if (Test-Path $asset.Path) {
    $existing = Get-Item $asset.Path
    $needsDownload = $existing.Length -lt $asset.MinBytes
  }

  if ($needsDownload) {
    if (Test-Path $asset.Path) {
      Remove-Item -Force $asset.Path
    }
    Write-Host "Downloading $($asset.Url)"
    & curl.exe -L --retry 5 --retry-delay 3 --retry-all-errors -o $asset.Path $asset.Url
    $downloaded = Get-Item $asset.Path
    if ($downloaded.Length -lt $asset.MinBytes) {
      throw "Downloaded file too small: $($downloaded.Name) ($($downloaded.Length) bytes)"
    }
  }
}

$coverChemistry = @"
<svg xmlns="http://www.w3.org/2000/svg" width="1200" height="675" viewBox="0 0 1200 675">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#1f2937"/>
      <stop offset="100%" stop-color="#9333ea"/>
    </linearGradient>
  </defs>
  <rect width="1200" height="675" fill="url(#bg)" rx="36"/>
  <circle cx="970" cy="170" r="115" fill="rgba(255,255,255,0.08)"/>
  <circle cx="880" cy="510" r="180" fill="rgba(255,255,255,0.05)"/>
  <text x="80" y="160" fill="#e9d5ff" font-size="34" font-family="Segoe UI, Arial, sans-serif">MIT OpenCourseWare</text>
  <text x="80" y="290" fill="#ffffff" font-size="58" font-weight="700" font-family="Segoe UI, Arial, sans-serif">Solid State Chemistry</text>
  <text x="80" y="380" fill="#f3e8ff" font-size="28" font-family="Segoe UI, Arial, sans-serif">Local demo import with downloadable lecture videos</text>
</svg>
"@

$coverNonlinear = @"
<svg xmlns="http://www.w3.org/2000/svg" width="1200" height="675" viewBox="0 0 1200 675">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#111827"/>
      <stop offset="100%" stop-color="#0f766e"/>
    </linearGradient>
  </defs>
  <rect width="1200" height="675" fill="url(#bg)" rx="36"/>
  <rect x="760" y="110" width="280" height="280" rx="28" fill="rgba(255,255,255,0.08)"/>
  <path d="M810 330 C860 210, 940 210, 990 330" stroke="#99f6e4" stroke-width="14" fill="none"/>
  <text x="80" y="160" fill="#99f6e4" font-size="34" font-family="Segoe UI, Arial, sans-serif">MIT OpenCourseWare</text>
  <text x="80" y="290" fill="#ffffff" font-size="58" font-weight="700" font-family="Segoe UI, Arial, sans-serif">Nonlinear Programming</text>
  <text x="80" y="380" fill="#d1fae5" font-size="28" font-family="Segoe UI, Arial, sans-serif">Local demo import with downloadable lecture videos</text>
</svg>
"@

[System.IO.File]::WriteAllText((Join-Path $coverDir 'mit-chemistry.svg'), $coverChemistry, [System.Text.UTF8Encoding]::new($false))
[System.IO.File]::WriteAllText((Join-Path $coverDir 'mit-nonlinear.svg'), $coverNonlinear, [System.Text.UTF8Encoding]::new($false))

docker exec youda-backend sh -lc "mkdir -p /app/uploads/open-course /app/uploads/course-cover"
docker cp (Join-Path $videoDir 'mit-chemistry-intro.mp4') youda-backend:/app/uploads/open-course/mit-chemistry-intro.mp4
docker cp (Join-Path $videoDir 'mit-chemistry-periodic-table.mp4') youda-backend:/app/uploads/open-course/mit-chemistry-periodic-table.mp4
docker cp (Join-Path $videoDir 'mit-nonlinear-10feb2004.mp4') youda-backend:/app/uploads/open-course/mit-nonlinear-10feb2004.mp4
docker cp (Join-Path $videoDir 'mit-nonlinear-03may2004.mp4') youda-backend:/app/uploads/open-course/mit-nonlinear-03may2004.mp4
docker cp (Join-Path $coverDir 'mit-chemistry.svg') youda-backend:/app/uploads/course-cover/mit-chemistry.svg
docker cp (Join-Path $coverDir 'mit-nonlinear.svg') youda-backend:/app/uploads/course-cover/mit-nonlinear.svg

$sql = @"
SET NAMES utf8mb4;

UPDATE course
SET name = 'MIT OCW: Solid State Chemistry',
    description = 'Downloadable MIT OpenCourseWare lecture videos stored locally for the demo environment.',
    cover_image = '/uploads/course-cover/mit-chemistry.svg',
    teacher_name = 'Donald Sadoway',
    subject_id = 5,
    grade_id = 12,
    chapter_count = 2,
    student_count = 5
WHERE id = 1;

UPDATE course
SET name = 'MIT OCW: Nonlinear Programming',
    description = 'Downloadable MIT OpenCourseWare lecture videos stored locally for the demo environment.',
    cover_image = '/uploads/course-cover/mit-nonlinear.svg',
    teacher_name = 'Robert M. Freund',
    subject_id = 2,
    grade_id = 12,
    chapter_count = 2,
    student_count = 4
WHERE id = 2;

UPDATE course_chapter SET course_id = 1, title = 'Course Introduction', sort = 1 WHERE id = 1;
UPDATE course_chapter SET course_id = 1, title = 'Periodic Table', sort = 2 WHERE id = 2;
UPDATE course_chapter SET course_id = 2, title = 'Lecture 10 Feb 2004', sort = 1 WHERE id = 3;
UPDATE course_chapter SET course_id = 2, title = 'Lecture 03 May 2004', sort = 2 WHERE id = 4;

UPDATE course_video
SET course_id = 1,
    chapter_id = 1,
    title = 'Lecture 1: Introduction to Solid State Chemistry',
    video_url = '/uploads/open-course/mit-chemistry-intro.mp4',
    duration = 0,
    sort = 1
WHERE id = 1;

UPDATE course_video
SET course_id = 1,
    chapter_id = 2,
    title = 'Lecture 2: The Periodic Table',
    video_url = '/uploads/open-course/mit-chemistry-periodic-table.mp4',
    duration = 0,
    sort = 1
WHERE id = 2;

UPDATE course_video
SET course_id = 2,
    chapter_id = 3,
    title = 'MIT OCW Nonlinear Programming - 10 Feb 2004',
    video_url = '/uploads/open-course/mit-nonlinear-10feb2004.mp4',
    duration = 0,
    sort = 1
WHERE id = 3;

UPDATE course_video
SET course_id = 2,
    chapter_id = 4,
    title = 'MIT OCW Nonlinear Programming - 03 May 2004',
    video_url = '/uploads/open-course/mit-nonlinear-03may2004.mp4',
    duration = 0,
    sort = 1
WHERE id = 4;

DELETE FROM learning_progress WHERE video_id NOT IN (1,2,3,4);
"@

$sqlFile = Join-Path $tempRoot 'import-open-courses.sql'
[System.IO.File]::WriteAllText($sqlFile, $sql, [System.Text.UTF8Encoding]::new($false))
Get-Content -Raw -Path $sqlFile | docker exec -i youda-mysql mysql -uroot -proot --default-character-set=utf8mb4 -D youda
Write-Host 'Open course import completed.'
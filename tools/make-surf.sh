#!/bin/bash
# Makes the surf of the beach: the four waves of a recording (wave_01.flac to wave_04.flac in the first argument) are put one
# after another with pauses between them, over a quiet wash of the water, into a loop of 40 seconds, and its two channels are
# written as two mono files (OpenAL plays only mono sounds in 3D: one is put to the left of the beach and one to the right).
# The files are ogg vorbis, which the engine reads, and the encoder of ffmpeg makes only stereo of it: the mono files are
# made by wav_to_vorbis.py (it needs the soundfile package of python).
#
#     make-surf.sh recordings-directory output-directory
set -euo pipefail

source="$1"
output="$2"
length=40

# The waves: the file, when it starts (milliseconds) and how loud it is. The last one ends well before the end, so the loop is quiet
# where it is closed
waves=("01 1000 1.0" "03 9000 0.9" "02 14500 1.0" "04 21500 1.0" "03 29000 0.7" "01 34000 0.85")

inputs=()
filters=""
mix=""
index=0
for wave in "${waves[@]}"; do
    read -r file start gain <<<"$wave"
    inputs+=(-i "$source/wave_$file.flac")
    duration=$(ffprobe -v error -show_entries format=duration -of csv=p=0 "$source/wave_$file.flac")
    filters+="[$index:a]afade=t=in:d=0.3,afade=t=out:st=$(echo "$duration - 0.6" | bc -l):d=0.6,volume=$gain,adelay=${start}|${start}[w$index];"
    mix+="[w$index]"
    index=$((index + 1))
done

# A quiet wash under the waves, with a slow swell (four of them in the loop, so it closes)
inputs+=(-f lavfi -i "anoisesrc=color=pink:amplitude=0.04:duration=$length:sample_rate=44100:seed=7")
filters+="[$index:a]lowpass=f=2500,highpass=f=120,tremolo=f=0.1:d=0.6,aformat=channel_layouts=stereo,afade=t=in:d=0.2,afade=t=out:st=$(echo "$length - 0.2" | bc -l):d=0.2[bed];"
mix+="[bed]"

filters+="${mix}amix=inputs=$((index + 1)):normalize=0,atrim=0:$length,loudnorm=I=-20:TP=-2:LRA=11,aresample=44100[all];[all]channelsplit=channel_layout=stereo[left][right]"

mkdir -p "$output"
temporary=$(mktemp -d)
ffmpeg -v error -y "${inputs[@]}" -filter_complex "$filters" -map "[left]" -ac 1 "$temporary/surf-left.wav" -map "[right]" -ac 1 "$temporary/surf-right.wav"

here="$(cd "$(dirname "$0")" && pwd)"
python3 "$here/wav_to_vorbis.py" "$temporary/surf-left.wav" "$output/surf-left.ogg"
python3 "$here/wav_to_vorbis.py" "$temporary/surf-right.wav" "$output/surf-right.ogg"
rm -r "$temporary"

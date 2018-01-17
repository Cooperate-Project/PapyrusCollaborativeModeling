#!/bin/sh
# ====================================================================
# Copyright (c) 2014, 2018 Obeo and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#    Obeo - initial API and implementation
#    Christian W. Damus - Adapt for Papyrus Compare releng
# ====================================================================

set -ux

# Get common function and values
source `dirname $0`/common.sh

######################################################################
# Constants
######################################################################

# The type of build being published
BUILD_TYPE=nightly
PROG=`basename $0`
REPOSITORY_PATH="plugins/compare/org.eclipse.papyrus.compare.p2/target/repository"
HELP=false

######################################################################
# Arguments processing
######################################################################

usage() {
  echo "Usage: "$PROG" [-h] -v version -i build-id -w workspace -r repository"
  echo "  Options:"
  echo "    -v, --version     The version to be published. Format is X.Y.Z where X, Y and Z are integer"
  echo "    -b, --build-type  The build type, one of n[ightly] (default), i[ntegration], or r[elease]"
  echo "    -i, --build-id    The build ID. Format is YYYY-MM-DD_HH-MM-SS"
  echo "    -w, --workspace   The folder where the artifacts have been built"
  echo "    -r, --repository  The repository folder to publish"
  echo "    -h, --help        Display this help message"
}

SHORT_OPT_STRING="hw:b:i:v:r:"
getopt -T > /dev/null
if [ $? -eq 4 ]; then
  # GNU enhanced getopt is available
  ARGS=`getopt --name "$PROG" --long help,workspace:,build-type:,build-id:,version:,repository: --options "$SHORT_OPT_STRING" -- "$@"`
else
  # Original getopt is available (no long option names, no whitespace, no sorting)
  ARGS=`getopt "$SHORT_OPT_STRING" "$@"`
fi
if [ $? -ne 0 ]; then
  usage >&2
  exit 2
fi
eval set -- $ARGS

while [ $# -gt 0 ]; do
    case "$1" in
        -h | --help)        HELP=true;;
        -w | --workspace)   WORKSPACE="$2"; shift;;
        -b | --build-type)  BUILD_TYPE="$2"; shift;;
        -i | --build-id)    BUILD_ID="$2"; shift;;
        -v | --version)     VERSION="$2"; shift;;
        -r | --repository)  REPOSITORY_PATH="$2"; shift;;
        --)                 shift; break;; # end of options
    esac
    shift
done

if [ "$HELP" = true ]; then
  usage
  exit 0
fi

if [ -z "$WORKSPACE" ]; then
  echo "$0: workspace argument is mandatory" >&2
  usage >&2 
  exit 1
fi

if [ ! -d "$WORKSPACE" ]; then
  echo "$0: workspace does not exist or is not a directory -- $WORKSPACE" >&2
  usage >&2 
  exit 1
fi

if [ -z "$REPOSITORY_PATH" ]; then
  echo "$0: repository argument is mandatory" >&2
  usage >&2 
  exit 1
fi

if [ ! -d "$REPOSITORY_PATH" ]; then
  echo "$0: repository does not exist or is not a directory -- $REPOSITORY_PATH" >&2
  usage >&2 
  exit 1
fi

if [ -z "$BUILD_TYPE" ]; then
  BUILD_TYPE=nightly
  BUILD_PREFIX=$NIGHTLY_PREFIX
else
  BUILD_TYPE=$(echo "$BUILD_TYPE" | tr '[:upper:]' '[:lower:]')
fi

case "$BUILD_TYPE" in
  n | nightly)		BUILD_PREFIX=$NIGHTLY_PREFIX;;
  i | integration)	BUILD_PREFIX=$INTEGRATION_PREFIX;;
  r | release)		BUILD_PREFIX=$RELEASE_PREFIX;;
  *) echo "$0: unknown build type '$BUILD_TYPE'" >&2; usage >&2; exit 1;;
esac

if [ -z "$BUILD_ID" ]; then
  echo "$0: build-id argument is mandatory" >&2
  usage >&2 
  exit 1
fi 

echo "$BUILD_ID" | grep -E '^[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}-[0-9]{2}$' > /dev/null
if [ $? -ne 0 ]; then
  echo "$0: bad build-id format -- $BUILD_ID" >&2 
  usage >&2 
  exit 1
fi

if [ -z "$VERSION" ]; then
  echo "$0: version argument is mandatory" >&2
  usage >&2 
  exit 1
fi 

echo $VERSION | grep -E '^[0-9]+\.[0-9]+\.[0-9]+$' > /dev/null
if [ $? -ne 0 ]; then
  echo "$0: bad version format -- $VERSION" >&2 
  usage >&2 
  exit 1
fi

# Exit on error
set -e

# update the "latest" repositories to the new build. Should be used as a callback in 
# function visitVersions
#  1/ the path
#  2/ the version of interrest
#  3/ the currently visited major version (an integer)
#  4/ the currently visited minor version (an integer)
#  5/ the currently visited micro version (an integer)
#  6/ the currently visited build (format x.y.z-TYYYYMMDD-HHMM, where x, y and z 
#     are integer, T is N for nightly, I for integration or R for release and YYYYMMDD-HHMM is a timestamp)
#  7/ the most recent major version in the given $1 path (an integer)
#  8/ the most recent minor version in the currently visited major version (an integer)
#  9/ the most recent micro version in the currently visited minor version (an integer)
#  10/ the most recent build version in the currently visited micro version (format x.y.z-TYYYYMMDD-HHMM, where x, y and z 
#     are integer, T is N for nightly, I for integration or R for release and YYYYMMDD-HHMM is a timestamp)
updateLatestRedirections() {
	local path=$1
	local versionToPublish=$2
	local visitedMajor=$3
	local visitedMinor=$4
	local visitedMicro=$5
	local visitedBuild=$6
	local latestMajor=$7
	local latestMinor=$8
	local latestMicro=$9
	local latestBuild=${10}
	
	local updateSiteURL="$PAPYRUS_COMPARE_UPDATES_BASE_URL/$BUILD_TYPE/$latestBuild"

	local nextMajor="$(($visitedMajor+1)).0.0"
	local nextMinor="$visitedMajor.$(($visitedMinor+1)).0"
	local nextMicro="$visitedMajor.$visitedMinor.$(($visitedMicro+1)).0"

	if [ $(compareOSGIVersions $versionToPublish "$visitedMajor.$visitedMinor.$visitedMicro") -ge 0 ]; then
		if [  $(compareOSGIVersions $versionToPublish $nextMicro) -lt 0 ]; then
				local stream="$visitedMajor.$visitedMinor.$visitedMicro"
				echo "    $PAPYRUS_COMPARE_UPDATES_BASE_URL/$BUILD_TYPE/$stream/latest"
				createRedirect "$path/$stream/latest" "$updateSiteURL" "Papyrus Compare latest $stream $BUILD_TYPE build"
		fi 

		if [ "$visitedMicro" -eq "$latestMicro" ]; then
			if [ $(compareOSGIVersions $versionToPublish $nextMinor) -lt 0 ]; then
				local stream="$visitedMajor.$visitedMinor.x"
				echo "    $PAPYRUS_COMPARE_UPDATES_BASE_URL/$BUILD_TYPE/$stream/latest"
				createRedirect "$path/$stream/latest" "$updateSiteURL" "Papyrus Compare latest $stream $BUILD_TYPE build"
			fi

			if [ "$visitedMinor" -eq "$latestMinor" ]; then
				if [ $(compareOSGIVersions $versionToPublish $nextMajor) -lt 0 ]; then
					local stream="$visitedMajor.x"
					echo "    $PAPYRUS_COMPARE_UPDATES_BASE_URL/$BUILD_TYPE/$stream/latest"
					createRedirect "$path/$stream/latest" "$updateSiteURL" "Papyrus Compare latest $stream $BUILD_TYPE build"
				fi

				if [ "$visitedMajor" -eq "$latestMajor" ]; then
					echo "    $PAPYRUS_COMPARE_UPDATES_BASE_URL/$BUILD_TYPE/latest"
					createRedirect "$path/latest" "$updateSiteURL" "Papyrus Compare latest $BUILD_TYPE build"
				fi
			fi
		fi
	fi

	return 0
}

######################################################################
# Setup
######################################################################

# MINOR_STREAMs are of the form 1.0.x: only keep major and minor version number parts
export MINOR_STREAM=$(minorStream "$VERSION")

# MINOR_STREAMs are of the form 1.x: only keep major version number parts
export MAJOR_STREAM=$(majorStream "$VERSION")

# Converts the Hudson BUILD_ID (e.g. 2013-10-15_07-07-07) into the
# syntax we want for our update-sites (e.g. 20131015-070707)
export BUILD_TIMESTAMP=$(buildTimestamp "$BUILD_ID")

# The full version for this build, e.g. 0.9.0-N20131015-070707
export FULL_VERSION="${VERSION}-${BUILD_PREFIX}${BUILD_TIMESTAMP}"

# The root folder where all the builds of the same type as this one
# are published
export BUILD_PATH="$PAPYRUS_COMPARE_UPDATES_ROOT/$BUILD_TYPE"

# The folder for this particular build
export UPDATE_SITE_PATH=

# The URL on which this particular build will be made available
export UPDATE_SITE_URL="$PAPYRUS_COMPARE_UPDATES_BASE_URL/$BUILD_TYPE/$FULL_VERSION"

######################################################################
# Publish the build
######################################################################

echo "Publishing build $BUILD_PATH/$FULL_VERSION to $UPDATE_SITE_URL"

# Ensure the target folder exists
mkdir -p "$BUILD_PATH/$FULL_VERSION"
# The actual publication of the p2 repo produced by the build
cp -a "$WORKSPACE/$REPOSITORY_PATH/"* "$BUILD_PATH/$FULL_VERSION"
# Also publish a dump of the build environment, may be useful to debug
env | sort > "$BUILD_PATH/$FULL_VERSION/build_env.txt"

echo "Adding $UPDATE_SITE_URL to composites repositories:"

# add a link for the $VERSION (e.g. "1.2.0" => "1.2.0-NYYYYMMDD-HHMM")
echo "    $PAPYRUS_COMPARE_UPDATES_BASE_URL/$BUILD_TYPE/$VERSION"
composite-repository \
	-location "$BUILD_PATH/$VERSION" \
	-add "$UPDATE_SITE_URL" \
	-repositoryName "Papyrus Compare $VERSION $BUILD_TYPE builds"
createP2Index "$BUILD_PATH/$VERSION"

# add a link for the $MINOR_STREAM (e.g. "1.2.x" => "1.2.0-NYYYYMMDD-HHMM")
echo "    $PAPYRUS_COMPARE_UPDATES_BASE_URL/$BUILD_TYPE/$MINOR_STREAM"
composite-repository \
	-location "$BUILD_PATH/$MINOR_STREAM" \
	-add "$UPDATE_SITE_URL" \
	-repositoryName "Papyrus Compare $MINOR_STREAM $BUILD_TYPE builds"
createP2Index "$BUILD_PATH/$MINOR_STREAM"

# add a link for the $MAJOR_STREAM (e.g. "1.x" => "1.2.0-NYYYYMMDD-HHMM")
echo "    $PAPYRUS_COMPARE_UPDATES_BASE_URL/$BUILD_TYPE/$MAJOR_STREAM"
composite-repository \
	-location "$BUILD_PATH/$MAJOR_STREAM" \
	-add "$UPDATE_SITE_URL" \
	-repositoryName "Papyrus Compare $MAJOR_STREAM $BUILD_TYPE builds"
createP2Index "$BUILD_PATH/$MAJOR_STREAM"

# add a link for all nightly list
echo "    $PAPYRUS_COMPARE_UPDATES_BASE_URL/$BUILD_TYPE"
composite-repository \
	-location "$BUILD_PATH" \
	-add "$UPDATE_SITE_URL" \
	-repositoryName "Papyrus Compare $BUILD_TYPE builds"
createP2Index "$BUILD_PATH"

# Setup or update the redirects (implemented as composite repos)
echo "Redirecting 'latest' repositories to $UPDATE_SITE_URL:"
visitVersions "$BUILD_PATH" "updateLatestRedirections" $VERSION

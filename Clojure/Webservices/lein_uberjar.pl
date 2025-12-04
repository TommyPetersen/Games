#!/usr/bin/env perl
use strict;
use warnings;

my $mappenavn = ".";

opendir DIR,$mappenavn;
my @dir = readdir(DIR);
my $underdirN1;
my $underdirN2;

foreach $underdirN1 (<$mappenavn/*>) {
    foreach $underdirN2 (<$underdirN1/*>) {
        if(-d $underdirN2) {
            systemkald("cd $underdirN2; lein uberjar;");
        };
    }
}
close DIR;

#
# Systemkaldsrutine
#
sub systemkald {
    system(@_) == 0
	or die "system @_ failed: $?";
}
